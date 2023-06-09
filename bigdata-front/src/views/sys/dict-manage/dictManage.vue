<style lang="less">
@import "@/styles/tree&table-common.less";
@import "./dictManage.less";
</style>
<template>
  <div class="search">
    <Card>
      <Row type="flex" justify="space-between">
        <Col v-show="expand" span="5">
          <Row class="operation">
            <Button @click="handleAddDict" type="primary" icon="md-add"
              >添加字典</Button
            >
            <Dropdown @on-click="handleDropdown">
              <Button>
                更多操作
                <Icon type="md-arrow-dropdown" />
              </Button>
              <DropdownMenu slot="list">
                <DropdownItem name="editDict">编辑字典</DropdownItem>
                <DropdownItem name="delDict">删除字典</DropdownItem>
                <DropdownItem name="refreshDict">刷新</DropdownItem>
              </DropdownMenu>
            </Dropdown>
          </Row>
          <Alert show-icon>
            当前选择：
            <span class="select-title">{{ editTitle }}</span>
            <a class="select-clear" v-if="editTitle" @click="cancelEdit"
              >取消选择</a
            >
          </Alert>
          <Input
            v-model="searchKey"
            suffix="ios-search"
            @on-change="search"
            placeholder="输入搜索字典"
            clearable
          />
          <div style="position: relative">
            <div class="tree-bar" :style="{ maxHeight: maxHeight }">
              <Tree
                ref="tree"
                :data="treeData"
                @on-select-change="selectTree"
              ></Tree>
            </div>
            <Spin size="large" fix v-if="treeLoading"></Spin>
          </div>
        </Col>
        <div class="expand">
          <Icon
            :type="expandIcon"
            size="16"
            class="icon"
            @click="changeExpand"
          />
        </div>
        <Col :span="span">
          <Row v-show="openSearch" @keydown.enter.native="handleSearch">
            <Form ref="searchForm" :model="searchForm" inline :label-width="70">
              <FormItem label="数据名称" prop="title">
                <Input
                  type="text"
                  v-model="searchForm.title"
                  placeholder="请输入"
                  clearable
                  style="width: 200px"
                />
              </FormItem>
              <FormItem label="状态" prop="status">
                <Select
                  v-model="searchForm.status"
                  placeholder="请选择"
                  clearable
                  style="width: 200px"
                >
                  <Option value="0">正常</Option>
                  <Option value="-1">禁用</Option>
                </Select>
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
                >添加数据</Button
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
        </Col>
      </Row>
    </Card>

    <addDictType
      :dataLength="dataLength"
      v-model="showAddDict"
      @on-submit="getAllDict"
    />

    <editDictType
      :data="dictForm"
      v-model="showEditDict"
      @on-submit="editDictSuccess"
    />

    <Modal
      :title="modalTitle"
      v-model="modalVisible"
      :mask-closable="false"
      :width="500"
    >
      <Form ref="form" :model="form" :label-width="80" :rules="formValidate">
        <FormItem label="名称" prop="title">
          <Input v-model="form.title" />
        </FormItem>
        <FormItem label="数据值" prop="value">
          <Input v-model="form.value" />
        </FormItem>
        <FormItem label="备注" prop="description">
          <Input v-model="form.description" />
        </FormItem>
        <FormItem label="排序值" prop="sortOrder">
          <Tooltip
            trigger="hover"
            placement="right"
            content="值越小越靠前，支持小数"
          >
            <InputNumber
              :max="1000"
              :min="0"
              v-model="form.sortOrder"
            ></InputNumber>
          </Tooltip>
        </FormItem>
        <FormItem label="是否启用" prop="status">
          <i-switch
            size="large"
            v-model="form.status"
            :true-value="0"
            :false-value="-1"
          >
            <span slot="open">启用</span>
            <span slot="close">禁用</span>
          </i-switch>
        </FormItem>
      </Form>
      <div slot="footer">
        <Button type="text" @click="modalVisible = false">取消</Button>
        <Button type="primary" :loading="submitLoading" @click="handelSubmit"
          >提交</Button
        >
      </div>
    </Modal>
  </div>
</template>

<script>
import {
  getAllDictList,
  deleteDict,
  searchDict,
  getAllDictDataList,
  addDictData,
  editDictData,
  deleteData,
} from "@/api/index";
import addDictType from "./addDictType.vue";
import editDictType from "./editDictType.vue";
export default {
  name: "dic-manage",
  components: {
    addDictType,
    editDictType,
  },
  data() {
    return {
      tableSize: "default",
      dataLength: 0,
      showAddDict: false,
      dictForm: {},
      showEditDict: false,
      openSearch: true,
      openTip: true,
      treeLoading: false, // 树加载状态
      maxHeight: "500px",
      loading: false, // 表格加载状态
      editTitle: "", // 编辑节点名称
      searchKey: "", // 搜索树
      expand: true,
      span: 18,
      expandIcon: "ios-arrow-back",
      selectNode: {},
      treeData: [], // 树数据
      selectList: [], // 多选数据
      searchForm: {
        // 搜索框对应data对象
        name: "",
        status: "",
        pageNumber: 1, // 当前页数
        pageSize: 10, // 页面大小
        sort: "sortOrder", // 默认排序字段
        order: "asc", // 默认排序方式
      },
      modalType: 0, // 添加或编辑标识
      modalVisible: false, // 添加或编辑显示
      modalTitle: "", // 添加或编辑标题
      form: {
        // 添加或编辑表单对象初始化数据
        title: "",
        value: "",
        status: 0,
        description: "",
        sortOrder: 0,
      },
      formValidate: {
        // 表单验证规则
        title: [{ required: true, message: "请输入", trigger: "blur" }],
        value: [{ required: true, message: "请输入", trigger: "blur" }],
        sortOrder: [
          {
            required: true,
            type: "number",
            message: "请输入排序值",
            trigger: "blur",
          },
        ],
      },
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
          title: "名称",
          key: "title",
          minWidth: 160,
          sortable: true,
        },
        {
          title: "数据值",
          key: "value",
          minWidth: 160,
          sortable: true,
        },
        {
          title: "备注",
          key: "description",
          width: 150,
          sortable: true,
        },
        {
          title: "排序值",
          key: "sortOrder",
          width: 100,
          align: "center",
          sortable: true,
          sortType: "asc",
        },
        {
          title: "状态",
          key: "status",
          align: "center",
          width: 120,
          render: (h, params) => {
            let re = "";
            if (params.row.status == 0) {
              return h("div", [
                h("Badge", {
                  props: {
                    status: "success",
                    text: "启用",
                  },
                }),
              ]);
            } else if (params.row.status == -1) {
              return h("div", [
                h("Badge", {
                  props: {
                    status: "error",
                    text: "禁用",
                  },
                }),
              ]);
            }
          },
        },
        {
          title: "创建时间",
          key: "createTime",
          width: 200,
          sortable: true,
        },
        {
          title: "操作",
          key: "action",
          width: 150,
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
      submitLoading: false, // 添加或编辑提交状态
      data: [], //表单数据
      total: 0, // 表单数据总数
    };
  },
  methods: {
    init() {
      // 获取树数据
      this.getAllDict();
      // 获取表单数据
      this.getDataList();
    },
    getAllDict() {
      this.treeLoading = true;
      getAllDictList().then((res) => {
        this.treeLoading = false;
        if (res.success) {
          this.treeData = res.result;
        }
      });
    },
    search() {
      // 搜索树
      if (this.searchKey) {
        this.treeLoading = true;
        searchDict({ key: this.searchKey }).then((res) => {
          this.treeLoading = false;
          if (res.success) {
            this.treeData = res.result;
          }
        });
      } else {
        // 为空重新加载
        this.getAllDict();
      }
    },
    selectTree(v) {
      if (v.length > 0) {
        // 转换null为""
        for (let attr in v[0]) {
          if (v[0][attr] == null) {
            v[0][attr] = "";
          }
        }
        let str = JSON.stringify(v[0]);
        let data = JSON.parse(str);
        this.selectNode = data;
        this.dictForm = data;
        this.editTitle = data.title + "(" + data.type + ")";
        // 重新加载表
        this.searchForm.pageNumber = 1;
        this.searchForm.pageSize = 10;
        this.getDataList();
      } else {
        this.cancelEdit();
      }
    },
    cancelEdit() {
      let data = this.$refs.tree.getSelectedNodes()[0];
      if (data) {
        data.selected = false;
      }
      // 取消选择后获取全部数据
      this.selectNode = {};
      this.editTitle = "";
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
    changeSelect(v) {
      this.selectList = v;
    },
    changeExpand() {
      this.expand = !this.expand;
      if (this.expand) {
        this.expandIcon = "ios-arrow-back";
        this.span = 18;
      } else {
        this.expandIcon = "ios-arrow-forward";
        this.span = 23;
      }
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
    getDataList() {
      this.loading = true;
      if (this.selectNode.id) {
        this.searchForm.dictId = this.selectNode.id;
      } else {
        delete this.searchForm.dictId;
      }
      if (!this.searchForm.status) {
        this.searchForm.status = "";
      }
      getAllDictDataList(this.searchForm).then((res) => {
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
    showSelect(e) {
      this.selectList = e;
    },
    clearSelectAll() {
      this.$refs.table.selectAll(false);
    },
    refreshDict() {
      this.getAllDict();
      this.selectNode = {};
      this.editTitle = "";
      this.getDataList();
    },
    handleDropdown(name) {
      if (name == "editDict") {
        if (!this.selectNode.id) {
          this.$Message.warning("您还未选择要编辑的字典");
          return;
        }
        this.handleEditDict();
      } else if (name == "delDict") {
        this.delDict();
      } else if (name == "refreshDict") {
        this.refreshDict();
      }
    },
    handleAddDict() {
      this.dataLength = this.treeData.length + 1;
      this.showAddDict = true;
    },
    handleEditDict() {
      this.showEditDict = true;
    },
    editDictSuccess(v) {
      for (let attr in v) {
        if (v[attr] == null) {
          v[attr] = "";
        }
      }
      let str = JSON.stringify(v);
      let data = JSON.parse(str);
      this.dictForm = data;
      this.editTitle = v.title + "(" + v.type + ")";
      this.getAllDict();
    },
    delDict() {
      if (!this.selectNode.id) {
        this.$Message.warning("您还未选择要删除的字典");
        return;
      }
      this.$Modal.confirm({
        title: "确认删除",
        loading: true,
        content: "您确认要删除字典 " + this.selectNode.title + " 及其所有数据?",
        onOk: () => {
          // 删除
          deleteDict({ ids: this.selectNode.id }).then((res) => {
            this.$Modal.remove();
            if (res.success) {
              this.$Message.success("操作成功");
              this.refreshDict();
            }
          });
        },
      });
    },
    add() {
      if (!this.selectNode.id) {
        this.$Message.warning("请先选择一个字典类别");
        return;
      }
      this.modalType = 0;
      this.modalTitle = "添加字典 " + this.editTitle + " 的数据";
      this.$refs.form.resetFields();
      this.form.sortOrder = this.data.length + 1;
      this.modalVisible = true;
    },
    edit(v) {
      this.modalType = 1;
      if (this.editTitle) {
        this.modalTitle = "编辑字典 " + this.editTitle + " 的数据";
      } else {
        this.modalTitle = "编辑字典数据";
      }
      this.$refs.form.resetFields();
      // 转换null为""
      for (let attr in v) {
        if (v[attr] == null) {
          v[attr] = "";
        }
      }
      let str = JSON.stringify(v);
      let data = JSON.parse(str);
      this.form = data;
      this.modalVisible = true;
    },
    handelSubmit() {
      this.$refs.form.validate((valid) => {
        if (valid) {
          this.submitLoading = true;
          if (this.modalType == 0) {
            // 添加 避免编辑后传入id等数据 记得删除
            delete this.form.id;
            this.form.dictId = this.selectNode.id;
            addDictData(this.form).then((res) => {
              this.submitLoading = false;
              if (res.success) {
                this.$Message.success("操作成功");
                this.getDataList();
                this.modalVisible = false;
              }
            });
          } else if (this.modalType == 1) {
            // 编辑
            editDictData(this.form).then((res) => {
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
    remove(v) {
      this.$Modal.confirm({
        title: "确认删除",
        content: "您确认要删除 " + v.title + " ?",
        loading: true,
        onOk: () => {
          // 删除
          deleteData({ ids: v.id }).then((res) => {
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
          // 批量删除
          deleteData({ ids: ids }).then((res) => {
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
    // 计算高度
    let height = document.documentElement.clientHeight;
    this.maxHeight = Number(height - 287) + "px";
    this.init();
  },
};
</script>