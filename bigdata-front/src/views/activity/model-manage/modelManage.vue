<style lang="less">
@import "@/styles/table-common.less";
@import "./modelManage.less";
</style>
<template>
  <div class="search">
    <Card>
      <Row v-show="openSearch" @keydown.enter.native="handleSearch">
        <Form ref="searchForm" :model="searchForm" inline :label-width="70">
          <FormItem label="模型名称" prop="name">
            <Input
              type="text"
              v-model="searchForm.name"
              placeholder="请输入名称"
              clearable
              style="width: 200px"
            />
          </FormItem>
          <FormItem label="标识Key" prop="modelKey">
            <Input
              type="text"
              v-model="searchForm.modelKey"
              placeholder="请输入标识"
              clearable
              style="width: 200px"
            />
          </FormItem>
          <FormItem style="margin-left: -35px" class="br">
            <Button @click="handleSearch" type="primary" icon="ios-search"
              >搜索</Button
            >
            <Button @click="handleReset">重置</Button>
          </FormItem>
        </Form>
      </Row>
      <Row align="middle" justify="space-between" class="operation">
        <div>
          <Button @click="add" type="primary" icon="md-add"
            >添加空白模型</Button
          >
          <Button @click="delAll" icon="md-trash">批量删除</Button>
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
            :content="openSearch ? '关闭搜索' : '开启搜索'"
            placement="top"
            transfer
          >
            <Icon
              type="ios-search"
              size="18"
              class="item tip"
              @click="openSearch = !openSearch"
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
                <DropdownItem :selected="tableSize == 'default'" name="default"
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
        sortable="custom"
        @on-sort-change="changeSort"
        @on-selection-change="showSelect"
        ref="table"
      ></Table>
      <Row type="flex" justify="end" class="page">
        <Page
          :current="searchForm.pageNumber"
          :total="total"
          :page-size="searchForm.pageSize"
          @on-change="changePage"
          @on-page-size-change="changePageSize"
          :page-size-opts="[10, 20, 50]"
          size="small"
          show-total
          show-elevator
          show-sizer
        ></Page>
      </Row>
    </Card>

    <Modal
      :title="modalTitle"
      v-model="modalVisible"
      :mask-closable="false"
      :width="500"
    >
      <Form ref="form" :model="form" :label-width="80" :rules="formValidate">
        <FormItem label="名称" prop="name">
          <Input v-model="form.name" />
        </FormItem>
        <FormItem label="标识Key" prop="modelKey">
          <Input v-model="form.modelKey" />
        </FormItem>
        <FormItem label="备注" prop="description">
          <Input v-model="form.description" />
        </FormItem>
      </Form>
      <div slot="footer">
        <Button type="text" @click="handelCancel">取消</Button>
        <Button type="primary" :loading="submitLoading" @click="handelSubmit"
          >提交</Button
        >
      </div>
    </Modal>

    <Modal
      :closable="false"
      @on-cancel="handleClose"
      v-model="showModeler"
      :mask-closable="false"
      footer-hide
      :width="900"
      :fullscreen="full"
      class="modeler"
    >
      <div slot="header">
        <div class="ivu-modal-header-inner">Activiti工作流在线流程设计编辑</div>
        <a @click="handleFull" class="modal-fullscreen">
          <Icon
            v-show="!full"
            type="ios-expand"
            class="model-fullscreen-icon"
          />
          <Icon
            v-show="full"
            type="ios-contract"
            class="model-fullscreen-icon"
          />
        </a>
        <a @click="handleClose" class="ivu-modal-close">
          <Icon type="ios-close" size="31" class="ivu-icon-ios-close" />
        </a>
      </div>
      <div :style="{ position: 'relative', height: modalHeight }">
        <iframe
          id="iframe"
          :src="modelerUrl"
          frameborder="0"
          width="100%"
          height="100%"
          scrolling="auto"
        ></iframe>
        <Spin fix size="large" v-if="modelerLoading"></Spin>
      </div>
    </Modal>
  </div>
</template>

<script>
import {
  getModelDataList,
  addModel,
  deployModel,
  exportModel,
  deleteModel,
} from "@/api/activiti";
import { getOtherSet } from "@/api/index";
export default {
  name: "model-manage",
  data() {
    return {
      tableSize: "default",
      full: true,
      modalHeight: "100%",
      openSearch: true,
      openTip: true,
      showModeler: false,
      modelerLoading: false,
      domain: "",
      modelerUrl: "",
      loading: true, // 表单加载状态
      selectList: [], // 多选数据
      searchForm: {
        // 搜索框对应data对象
        name: "",
        modelKey: "",
        pageNumber: 1, // 当前页数
        pageSize: 10, // 页面大小
        sort: "createTime", // 默认排序字段
        order: "desc", // 默认排序方式
      },
      modalType: 0, // 添加或编辑标识
      modalVisible: false, // 添加或编辑显示
      modalTitle: "", // 添加或编辑标题
      form: {
        name: "",
        modelKey: "",
        description: "",
      },
      formValidate: {
        // 表单验证规则
        name: [{ required: true, message: "请输入", trigger: "change" }],
        modelKey: [{ required: true, message: "请输入", trigger: "change" }],
      },
      submitLoading: false, // 添加或编辑提交状态
      columns: [
        // 表头
        {
          type: "selection",
          width: 60,
          align: "center",
          fixed: "left",
        },
        {
          type: "index",
          width: 60,
          align: "center",
          fixed: "left",
        },
        {
          title: "名称",
          key: "name",
          minWidth: 180,
          sortable: true,
          fixed: "left",
        },
        {
          title: "标识Key",
          key: "modelKey",
          minWidth: 150,
          sortable: true,
        },
        {
          title: "备注描述",
          key: "description",
          minWidth: 150,
          sortable: true,
        },
        {
          title: "版本",
          key: "version",
          width: 130,
          align: "center",
          sortable: true,
          render: (h, params) => {
            let re = "";
            if (params.row.version) {
              re = "v." + params.row.version;
            }
            return h("div", re);
          },
        },
        {
          title: "创建时间",
          key: "createTime",
          width: 180,
          sortable: true,
          sortType: "desc",
        },
        {
          title: "更新时间",
          key: "updateTime",
          width: 180,
          sortable: true,
        },
        {
          title: "操作",
          key: "action",
          width: 300,
          align: "center",
          fixed: "right",
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
                "在线设计"
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
                      this.deploy(params.row);
                    },
                  },
                },
                "部署发布"
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
                      this.export(params.row);
                    },
                  },
                },
                "导出XML"
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
      total: 0, // 表单数据总数
    };
  },
  methods: {
    init() {
      this.getDataList();
      this.getDomain();
    },
    getDomain() {
      getOtherSet().then((res) => {
        if (res.result) {
          this.domain = res.result.domain;
        }
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
    changeTableSize(v) {
      this.tableSize = v;
    },
    exportData() {
      this.$refs.table.exportCsv({
        filename: "数据",
      });
    },
    getDataList() {
      this.loading = true;
      getModelDataList(this.searchForm).then((res) => {
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
    handleSearch() {
      this.searchForm.pageNumber = 1;
      this.searchForm.pageSize = 10;
      this.getDataList();
    },
    handleReset() {
      this.$refs.searchForm.resetFields();
      this.searchForm.pageNumber = 1;
      this.searchForm.pageSize = 10;
      // 重新加载数据
      this.getDataList();
    },
    changeSort(e) {
      this.searchForm.sort = e.key;
      this.searchForm.order = e.order;
      if (e.order == "normal") {
        this.searchForm.order = "";
      }
      this.getDataList();
    },
    handelCancel() {
      this.modalVisible = false;
    },
    handelSubmit() {
      this.$refs.form.validate((valid) => {
        if (valid) {
          this.submitLoading = true;
          if (this.modalType == 0) {
            // 添加 避免编辑后传入id等数据 记得删除
            delete this.form.id;
            addModel(this.form).then((res) => {
              this.submitLoading = false;
              if (res.success) {
                this.$Message.success("操作成功");
                this.getDataList();
                this.modalVisible = false;
              }
            });
          }
        }
      });
    },
    add() {
      this.modalType = 0;
      this.modalTitle = "添加空白模型";
      this.$refs.form.resetFields();
      this.modalVisible = true;
    },
    edit(v) {
      if (!this.domain) {
        this.$Modal.confirm({
          title: "您还未配置访问域名",
          content:
            "您还未配置应用部署访问域名，是否现在立即去配置?（配置后请刷新该页面）",
          onOk: () => {
            this.$router.push({
              name: "setting",
              query: { name: "other" },
            });
          },
        });
        return;
      }
      this.modelerUrl = `${this.domain}/modeler/modeler.html?modelId=${
        v.id
      }&accessToken=${this.getStore("accessToken")}&time=${new Date()}`;
      this.showModeler = true;
      this.modelerLoading = true;
      let that = this;
      // 判断iframe是否加载完毕
      let iframe = document.getElementById("iframe");
      if (iframe.attachEvent) {
        iframe.attachEvent("onload", function () {
          //iframe加载完成后你需要进行的操作
          that.modelerLoading = false;
        });
      } else {
        iframe.onload = function () {
          //iframe加载完成后你需要进行的操作
          that.modelerLoading = false;
        };
      }
    },
    handleFull() {
      this.full = !this.full;
      if (this.full) {
        this.modalHeight = "100%";
      } else {
        this.modalHeight = "550px";
      }
    },
    handleClose() {
      this.$Modal.confirm({
        title: "确认关闭",
        content: "请记得点击左上角保存按钮，确定关闭编辑器?",
        onOk: () => {
          this.getDataList();
          this.showModeler = false;
        },
      });
    },
    deploy(v) {
      this.$Modal.confirm({
        title: "确认部署发布",
        content: "您确认要部署发布模型 " + v.name + " ?",
        loading: true,
        onOk: () => {
          deployModel(v.id).then((res) => {
            this.$Modal.remove();
            if (res.success) {
              setTimeout(() => {
                this.showJump();
              }, 300);
            }
          });
        },
      });
    },
    showJump() {
      this.$Modal.confirm({
        title: "部署发布成功",
        content:
          "建议立即去分配审批节点人员并编辑流程信息，是否立即跳转查看该流程?",
        onOk: () => {
          this.$router.push({
            name: "process-manage",
          });
        },
      });
    },
    export(v) {
      window.open(
        exportModel + v.id + "?accessToken=" + this.getStore("accessToken")
      );
    },
    remove(v) {
      this.$Modal.confirm({
        title: "确认删除",
        content: "您确认要删除 " + v.name + " ?",
        loading: true,
        onOk: () => {
          deleteModel({ ids: v.id }).then((res) => {
            this.$Modal.remove();
            if (res.success) {
              this.clearSelectAll();
              this.$Message.success("操作成功");
              this.getDataList();
            }
          });
        },
      });
    },
    showSelect(e) {
      this.selectList = e;
    },
    clearSelectAll() {
      this.$refs.table.selectAll(false);
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
          let ids = "";
          this.selectList.forEach(function (e) {
            ids += e.id + ",";
          });
          ids = ids.substring(0, ids.length - 1);
          deleteModel({ ids: ids }).then((res) => {
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
  watch: {
    // 监听路由变化
    $route(to, from) {
      if (to.name == "model-manage") {
        this.getDataList();
      }
    },
  },
};
</script>