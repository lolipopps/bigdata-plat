<template>
  <div class="search">
    <Card>
      <Tabs v-model="tabName" :animated="false" @on-click="handleClickTab">
        <TabPane name="redis" label="Redis管理">
          <Row align="middle" justify="space-between" class="operation">
            <div>
              <Button @click="add" type="primary" icon="md-add">添加</Button>
              <Dropdown @on-click="handleDropdown">
                <Button>
                  更多操作
                  <Icon type="md-arrow-dropdown" />
                </Button>
                <DropdownMenu slot="list">
                  <DropdownItem name="delAll">批量删除</DropdownItem>
                  <DropdownItem name="clear">清空全部</DropdownItem>
                </DropdownMenu>
              </Dropdown>
              <Input
                v-model="searchKey"
                suffix="ios-search"
                @on-change="search"
                placeholder="输入Key搜索"
                clearable
                style="width: 250px"
              />
            </div>
            <div class="icons">
              <Tooltip content="刷新" placement="top" transfer>
                <Icon
                  type="md-refresh"
                  size="18"
                  class="item"
                  @click="getDataList"
                />
              </Tooltip>
              <Tooltip
                :content="openTip ? '关闭提示' : '开启提示'"
                placement="top"
                transfer
              >
                <Icon
                  type="md-bulb"
                  size="18"
                  class="item tip"
                  @click="openTip = !openTip"
                />
              </Tooltip>
              <Tooltip content="表格密度" placement="top" transfer>
                <Dropdown @on-click="changeTableSize" trigger="click">
                  <Icon type="md-list" size="18" class="item" />
                  <DropdownMenu slot="list">
                    <DropdownItem
                      :selected="tableSize == 'default'"
                      name="default"
                      >默认</DropdownItem
                    >
                    <DropdownItem :selected="tableSize == 'large'" name="large"
                      >宽松</DropdownItem
                    >
                    <DropdownItem :selected="tableSize == 'small'" name="small"
                      >紧密</DropdownItem
                    >
                  </DropdownMenu>
                </Dropdown>
              </Tooltip>
              <Tooltip content="导出数据" placement="top" transfer>
                <Icon
                  type="md-download"
                  size="18"
                  class="item"
                  @click="exportData"
                />
              </Tooltip>
            </div>
          </Row>
          <Alert show-icon v-show="openTip">
            已选择
            <span class="select-count">{{ selectList.length }}</span> 项
            <a class="select-clear" @click="clearSelectAll">清空</a>
          </Alert>
          <Table
            :loading="loading"
            border
            :columns="columns"
            :data="data"
            :size="tableSize"
            ref="table"
            @on-selection-change="changeSelect"
          ></Table>
          <Row type="flex" justify="end" class="page">
            <Page
              :current="pageNumber"
              :total="total"
              :page-size="pageSize"
              @on-change="changePage"
              @on-page-size-change="changePageSize"
              :page-size-opts="[10, 20, 50]"
              size="small"
              show-total
              show-elevator
              show-sizer
            ></Page>
          </Row>
        </TabPane>
        <TabPane name="monitor" label="Redis监控">
          <redis-monitor v-if="tabName=='monitor'"/>
        </TabPane>
      </Tabs>
    </Card>

    <Modal
      :title="modalTitle"
      v-model="modalVisible"
      :mask-closable="false"
      :width="500"
    >
      <Form ref="form" :model="form" :label-width="80" :rules="formValidate">
        <FormItem label="Key" prop="key">
          <Input v-model="form.key" style="width: 100%" />
        </FormItem>
        <FormItem label="Value" prop="value">
          <Input
            v-model="form.value"
            type="textarea"
            :rows="5"
            style="width: 100%"
          />
        </FormItem>
        <FormItem label="过期时间" prop="expireTime">
          <Tooltip trigger="hover" placement="right" content="永久不过期设为-1">
            <InputNumber :min="-1" v-model="form.expireTime"></InputNumber>
          </Tooltip>
          <span style="margin-left: 5px">秒</span>
        </FormItem>
      </Form>
      <div slot="footer">
        <Button type="text" @click="modalVisible = false">取消</Button>
        <Button type="primary" :loading="submitLoading" @click="handleSubmit"
          >提交</Button
        >
      </div>
    </Modal>
  </div>
</template>

<script>
import {
  getRedisData,
  saveRedis,
  deleteRedis,
  deleteAllRedis,
  getRedisByKey,
  getRedisKeySize,
} from "@/api/index";
import redisMonitor from "./redisMonitor.vue";
export default {
  name: "redis-notCache",
  components: {
    redisMonitor,
  },
  data() {
    return {
      tabName: "redis",
      searchKey: "",
      tableSize: "default",
      openTip: true,
      loading: true, // 表单加载状态
      modalType: 0, // 添加或编辑标识
      modalVisible: false, // 添加或编辑显示
      modalTitle: "", // 添加或编辑标题
      searchForm: {
        // 搜索框初始化对象
        pageNumber: 1, // 当前页数
        pageSize: 10, // 页面大小
      },
      form: {
        // 添加或编辑表单对象初始化数据
        key: "",
        value: "",
        expireTime: null,
      },
      // 表单验证规则
      formValidate: {
        key: [{ required: true, message: "请输入", trigger: "change" }],
        value: [{ required: true, message: "请输入", trigger: "change" }],
        expireTime: [
          {
            required: true,
            type: "number",
            message: "请输入",
            trigger: "change",
          },
        ],
      },
      submitLoading: false, // 添加或编辑提交状态
      selectList: [], // 多选数据
      columns: [
        // 表头
        {
          type: "selection",
          width: 60,
          align: "center",
        },
        {
          type: "index",
          width: 60,
          align: "center",
        },
        {
          title: "Key",
          key: "key",
          minWidth: 200,
          sortable: true,
        },
        {
          title: "Value",
          key: "value",
          minWidth: 400,
          ellipsis: true,
          sortable: true,
        },
        {
          title: "过期时间(秒)",
          key: "expireTime",
          width: 150,
          sortable: true,
        },
        {
          title: "操作",
          key: "action",
          align: "center",
          fixed: "right",
          width: 150,
          render: (h, params) => {
            return h("div", [
              h(
                "a",
                {
                  on: {
                    click: () => {
                      this.edit(params.row);
                    },
                  },
                },
                "编辑"
              ),
              h("Divider", {
                props: {
                  type: "vertical",
                },
              }),
              h(
                "a",
                {
                  on: {
                    click: () => {
                      this.remove(params.row);
                    },
                  },
                },
                "删除"
              ),
            ]);
          },
        },
      ],
      data: [], // 表单数据
      pageNumber: 1, // 当前页数
      pageSize: 10, // 页面大小
      total: 0, // 表单数据总数
    };
  },
  methods: {
    init() {
      // 键值
      getRedisKeySize().then((res) => {
        let size = res.result.keySize;
        if (size > 100000) {
          this.$Notice.info({
            title: "提示",
            desc: "检测到存储的数据已超过10万条，为避免加载过多数据当前仅显示前10万条数据",
          });
        }
      });
      this.getDataList();
    },
    handleClickTab(name) {
      if (name == "monitor") {
        this.$Message.info("每隔5秒刷新一次数据，请耐心等待图表绘制");
      }
    },
    handleDropdown(v) {
      if (v == "delAll") {
        this.delAll();
      } else if (v == "clear") {
        this.clear();
      }
    },
    changeTableSize(v) {
      this.tableSize = v;
    },
    exportData() {
      this.$refs.table.exportCsv({
        filename: "数据",
      });
    },
    changePage(v) {
      this.searchForm.pageNumber = v;
      this.getDataList();
      this.clearSelectAll();
    },
    changePageSize(v) {
      this.searchForm.pageSize = v;
      this.getDataList();
    },
    search() {
      this.searchForm.key = this.searchKey;
      this.searchForm.pageNumber = 1;
      this.searchForm.pageSize = 10;
      this.getDataList();
    },
    clearSelectAll() {
      this.$refs.table.selectAll(false);
    },
    changeSelect(e) {
      this.selectList = e;
    },
    getDataList() {
      this.loading = true;
      getRedisData(this.searchForm).then((res) => {
        this.loading = false;
        if (res.success) {
          this.data = res.result.content;
          this.total = res.result.totalElements;
          if (this.data.length == 0 && this.searchForm.pageNumber > 1) {
            this.searchForm.pageNumber -= 1;
            this.getDataList();
          }
        }
      });
    },
    handleSubmit() {
      this.$refs.form.validate((valid) => {
        if (valid) {
          this.submitLoading = true;
          // 添加或编辑
          saveRedis(this.form).then((res) => {
            this.submitLoading = false;
            if (res.success) {
              this.$Message.success("操作成功");
              this.getDataList();
              this.modalVisible = false;
            }
          });
        }
      });
    },
    add() {
      this.modalType = 0;
      this.modalTitle = "添加";
      this.$refs.form.resetFields();
      delete this.form.id;
      this.modalVisible = true;
    },
    edit(v) {
      if (v.value.indexOf("非字符") > -1) {
        this.$Message.warning("非字符格式数据暂不支持编辑");
        return;
      }
      this.modalType = 1;
      this.modalTitle = "编辑";
      this.$refs.form.resetFields();
      // 转换null为""
      for (let attr in v) {
        if (v[attr] == null) {
          v[attr] = "";
        }
      }
      let str = JSON.stringify(v);
      let data = JSON.parse(str);
      this.form.value = "读取中...";
      this.form.expireTime = null;
      getRedisByKey(data.key).then((res) => {
        this.form.value = "";
        if (res.success) {
          data.value = res.result.value;
          data.expireTime = res.result.expireTime;
          this.form = data;
        }
      });
      this.modalVisible = true;
    },
    clear() {
      this.$Modal.confirm({
        title: "请谨慎进行此操作！",
        content: "您确认要彻底清空删除所有数据?",
        loading: true,
        onOk: () => {
          // 删除
          deleteAllRedis().then((res) => {
            this.$Modal.remove();
            if (res.success) {
              this.$Message.success("操作成功");
              this.getDataList();
            }
          });
        },
      });
    },
    remove(v) {
      this.$Modal.confirm({
        title: "确认删除",
        // 记得确认修改此处
        content: "您确认要删除 " + v.key + " ?",
        loading: true,
        onOk: () => {
          // 删除
          deleteRedis({ keys: v.key }).then((res) => {
            this.$Modal.remove();
            if (res.success) {
              this.$Message.success("操作成功");
              this.clearSelectAll();
              this.getDataList();
            }
          });
        },
      });
    },
    delAll() {
      if (this.selectList.length <= 0) {
        this.$Message.warning("您还未选择要删除的数据");
        return;
      }
      this.$Modal.confirm({
        title: "确认删除",
        content: "您确认要删除所选的 " + this.selectList.length + " 条数据?",
        loading: true,
        onOk: () => {
          let keys = "";
          this.selectList.forEach(function (e) {
            keys += e.key + ",";
          });
          keys = keys.substring(0, keys.length - 1);
          // 批量删除
          deleteRedis({ keys: keys }).then((res) => {
            this.$Modal.remove();
            if (res.success) {
              this.$Message.success("操作成功");
              this.clearSelectAll();
              this.getDataList();
            }
          });
        },
      });
    },
  },
  mounted() {
    this.init();
  },
};
</script>
<style lang="less">
@import "@/styles/table-common.less";
</style>