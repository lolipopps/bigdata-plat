<style lang="less">
@import "@/styles/table-common.less";
.userIndex {
  z-index: 9999;
}
</style>
<template>
  <div class="search">
    <Card>
      <Row v-show="openSearch" @keydown.enter.native="handleSearch">
        <Form ref="searchForm" :model="searchForm" inline :label-width="70">
          <FormItem label="名称" prop="name">
            <Input
              type="text"
              v-model="searchForm.name"
              placeholder="请输入"
              clearable
              style="width: 200px"
            />
          </FormItem>
          <FormItem label="所属类别" prop="category">
            <Cascader
              v-model="selectCat"
              :data="category"
              :load-data="loadData"
              @on-change="handleChangeCat"
              change-on-select
              filterable
              placeholder="请选择或输入搜索类别"
              style="width: 200px"
            ></Cascader>
          </FormItem>
          <span v-if="drop">
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
            <FormItem label="创建时间">
              <DatePicker
                :options="options"
                v-model="selectDate"
                type="daterange"
                format="yyyy-MM-dd"
                clearable
                @on-change="selectDateRange"
                placeholder="选择起始时间"
                style="width: 200px"
              ></DatePicker>
            </FormItem>
          </span>
          <FormItem style="margin-left: -35px" class="br">
            <Button @click="handleSearch" type="primary" icon="ios-search"
              >搜索</Button
            >
            <Button @click="handleReset">重置</Button>
            <a class="drop-down" @click="dropDown">
              {{ dropDownContent }}
              <Icon :type="dropDownIcon"></Icon>
            </a>
          </FormItem>
        </Form>
      </Row>
      <Row align="middle" justify="space-between" class="operation">
        <div>
          <Button @click="add" type="primary" icon="md-add">添加</Button>
          <Button @click="delAll" icon="md-trash">批量删除</Button>
          <Dropdown @on-click="handleDropdown">
            <Button>
              更多操作
              <Icon type="md-arrow-dropdown" />
            </Button>
            <DropdownMenu slot="list">
              <DropdownItem name="exportData">导出所选数据</DropdownItem>
              <DropdownItem name="exportAll">导出全部数据</DropdownItem>
            </DropdownMenu>
          </Dropdown>
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
      <Table
        :columns="exportColumns"
        :data="exportData"
        ref="exportTable"
        style="display: none"
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
      :z-index="800"
      :width="500"
    >
      <Form ref="form" :model="form" :label-width="70" :rules="formValidate">
        <FormItem label="名称" prop="name">
          <Input v-model="form.name" autocomplete="off" />
        </FormItem>
        <FormItem
          label="密码"
          prop="password"
          v-if="modalType == 0"
          :error="errorPass"
        >
          <Input type="password" v-model="form.password" autocomplete="off" />
        </FormItem>
        <FormItem label="头像" prop="avatar" class="form-noheight">
          <upload-pic-input
            v-model="form.avatar"
            ref="upload"
          ></upload-pic-input>
        </FormItem>
        <FormItem label="关联用户" prop="userId">
          <userSelect v-model="form.userId" :maskStyle="{ zIndex: 9998 }" className="userIndex"/>
        </FormItem>
        <FormItem label="图片资料" prop="pic">
          <uploadPicThumb v-model="form.pic" />
        </FormItem>
        <FormItem label="所属类别" prop="categoryTitle" class="block-pop">
          <Poptip
            trigger="click"
            placement="right"
            title="选择类别"
            width="250"
          >
            <div style="display: flex">
              <Input
                v-model="form.categoryTitle"
                readonly
                style="margin-right: 10px"
              />
              <Button icon="md-trash" @click="clearSelectCat">清空已选</Button>
            </div>
            <div slot="content">
              <Tree
                :data="dataCat"
                :load-data="loadDataTree"
                @on-select-change="selectTree"
              ></Tree>
              <Spin size="large" fix v-if="loading"></Spin>
            </div>
          </Poptip>
        </FormItem>
        <FormItem label="类型" prop="type">
          <Select v-model="form.type" placeholder="请选择">
            <Option :value="0">类型1</Option>
            <Option :value="1">类型2</Option>
          </Select>
        </FormItem>
      </Form>
      <div slot="footer">
        <Button type="text" @click="handleCancel">取消</Button>
        <Button type="primary" :loading="submitLoading" @click="handleSubmit"
          >提交</Button
        >
      </div>
    </Modal>
    <Modal
      v-model="modalExportAll"
      title="确认导出"
      :loading="loadingExport"
      @on-ok="exportAll"
    >
      <p>您确认要导出全部 {{ total }} 条数据？</p>
    </Modal>
  </div>
</template>

<script>
import uploadPicInput from "@/views/my-components/bigdata/upload-pic-input";
import userSelect from "@/views/my-components/bigdata/user-select";
import uploadPicThumb from "@/views/my-components/bigdata/upload-pic-thumb";
import { shortcuts } from "@/libs/shortcuts";
export default {
  name: "complex-table",
  components: {
    uploadPicInput,
    userSelect,
    uploadPicThumb,
  },
  data() {
    return {
      tableSize: "default",
      openSearch: true, // 显示搜索
      openTip: true, // 显示提示
      loading: true, // 表单加载状态
      modalExportAll: false, // 显示导出全部数据
      loadingExport: true, // 导出全部数据状态
      drop: false, // 搜索展开标识
      dropDownContent: "展开", // 搜索展开标识文字
      dropDownIcon: "ios-arrow-down", //搜索展开图标
      selectList: [], // 多选数据
      viewImage: false, // 图片预览标识
      category: [
        // 搜索类别数据
      ],
      selectCat: [], // 搜索选择类别modal
      dataCat: [], // 编辑添加类别数据
      searchForm: {
        // 搜索框对应data对象
        name: "",
        categoryId: "",
        status: "",
        pageNumber: 1, // 当前页数
        pageSize: 10, // 页面大小
        sort: "createTime", // 默认排序字段
        order: "desc", // 默认排序方式
        startDate: "", // 起始时间
        endDate: "", // 终止时间
      },
      selectDate: null, // 选择日期绑定modal
      options: {
        shortcuts: shortcuts,
      },
      modalType: 0, // 添加或编辑标识
      modalVisible: false, // 添加或编辑显示
      modalTitle: "", // 添加或编辑标题
      form: {
        // 添加或编辑表单对象初始化数据
        type: 0,
        avatar: "https://s1.ax1x.com/2018/05/19/CcdVQP.png",
        categoryId: "",
        categoryTitle: "",
      },
      errorPass: "", // 密码错误提示
      formValidate: {
        // 表单验证规则
        name: [{ required: true, message: "请输入", trigger: "blur" }],
      },
      submitLoading: false, // 添加或编辑提交状态
      columns: [
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
          minWidth: 150,
          sortable: true,
          fixed: "left",
        },
        {
          title: "头像",
          key: "avatar",
          width: 150,
          align: "center",
          render: (h, params) => {
            return h("Avatar", {
              props: {
                src: params.row.avatar,
              },
            });
          },
        },
        {
          title: "所属类别",
          key: "categoryTitle",
          width: 150,
        },
        {
          title: "类型",
          key: "type",
          align: "center",
          width: 150,
          render: (h, params) => {
            let re = "";
            if (params.row.type == 0) {
              re = "类型1";
            } else if (params.row.type == 1) {
              re = "类型2";
            }
            return h("div", re);
          },
        },
        {
          title: "状态",
          key: "status",
          align: "center",
          width: 150,
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
          filters: [
            {
              label: "启用",
              value: 0,
            },
            {
              label: "禁用",
              value: -1,
            },
          ],
          filterMultiple: false,
          filterMethod(value, row) {
            return row.status == value;
          },
        },
        {
          title: "创建时间",
          key: "createTime",
          sortable: true,
          sortType: "desc",
          width: 180,
        },
        {
          title: "操作",
          key: "action",
          width: 200,
          align: "center",
          fixed: "right",
          render: (h, params) => {
            let enableOrDisable = "";
            if (params.row.status == 0) {
              enableOrDisable = h(
                "a",
                {
                  on: {
                    click: () => {
                      this.disable(params.row);
                    },
                  },
                },
                "禁用"
              );
            } else {
              enableOrDisable = h(
                "a",
                {
                  on: {
                    click: () => {
                      this.enable(params.row);
                    },
                  },
                },
                "启用"
              );
            }
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
              enableOrDisable,
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
      exportColumns: [
        {
          title: "名称",
          key: "name",
        },
        {
          title: "头像",
          key: "avatar",
        },
        {
          title: "所属类别",
          key: "categoryTitle",
        },
        {
          title: "类型",
          key: "type",
        },
        {
          title: "状态",
          key: "status",
        },
        {
          title: "创建时间",
          key: "createTime",
        },
      ],
      data: [], // 表单数据
      exportData: [], // 导出数据
      total: 0, // 表单数据总数
    };
  },
  methods: {
    init() {
      this.getDataList();
      // 初始化搜索框级联分类数据
      this.initCategoryData();
      // 初始化编辑添加树形分类数据
      this.initCategoryForEdit();
    },
    initCategoryData() {
      // this.getRequest("级联一级数据请求路径，如/category/getByParentId/0").then(res => {
      //   if (res.success) {
      //     res.result.forEach(function(e) {
      //       if (e.isParent) {
      //         e.value = e.id;
      //         e.label = e.title;
      //         e.loading = false;
      //         e.children = [];
      //       } else {
      //         e.value = e.id;
      //         e.label = e.title;
      //       }
      //     });
      //     this.category = res.result;
      //   }
      // });
      // 模拟请求成功
      this.category = [
        {
          label: "分类1",
          value: "1",
          loading: false,
          children: [
            {
              label: "二级分类",
              value: "1.1",
            },
          ],
        },
        {
          label: "分类2",
          value: "2",
        },
      ];
    },
    initCategoryForEdit() {
      // this.getRequest("树形一级数据请求路径，如/category/getByParentId/0").then(res => {
      //   if (res.success) {
      //     res.result.forEach(function(e) {
      //       if (e.isParent) {
      //         e.loading = false;
      //         e.children = [];
      //       }
      //     });
      //     this.dataCat = res.result;
      //   }
      // });
      // 模拟请求成功
      this.dataCat = [
        {
          title: "分类1",
          id: "1",
          loading: false,
          children: [
            {
              title: "二级分类",
              id: "2",
            },
          ],
        },
        {
          title: "分类2",
          id: "3",
        },
      ];
    },
    loadData(item, callback) {
      // 异步加载搜索框级联类别子节点数据
      // item.loading = true;
      // this.getRequest("请求路径，如/category/getByParentId/" + item.value).then(res => {
      //   item.loading = false;
      //   if (res.success) {
      //     res.result.forEach(function(e) {
      //       if (e.isParent) {
      //         e.value = e.id;
      //         e.label = e.title;
      //         e.loading = false;
      //         e.children = [];
      //       } else {
      //         e.value = e.id;
      //         e.label = e.title;
      //       }
      //       if (e.status == -1) {
      //         e.label = "[已禁用] " + e.label;
      //         e.disabled = true;
      //       }
      //     });
      //     item.children = res.result;
      //     callback();
      //   }
      // });
    },
    loadDataTree(item, callback) {
      // 异步加载编辑添加选择类别树子节点数据
      // this.getRequest("请求路径，如/category/getByParentId/" + item.id).then(res => {
      //   if (res.success) {
      //     res.result.forEach(function(e) {
      //       if (e.isParent) {
      //         e.loading = false;
      //         e.children = [];
      //       }
      //     });
      //     callback(res.result);
      //   }
      // });
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
        this.form.categoryId = data.id;
        this.form.categoryTitle = data.title;
      }
    },
    clearSelectCat() {
      this.form.categoryId = "";
      this.form.categoryTitle = "";
    },
    handleChangeCat(value, selectedData) {
      // 获取最后一个值
      if (value && value.length > 0) {
        this.searchForm.categoryId = value[value.length - 1];
      } else {
        this.searchForm.categoryId = "";
      }
    },
    handleChangeUserFormDep(value, selectedData) {
      // 获取最后一个值
      if (value && value.length > 0) {
        this.form.categoryId = value[value.length - 1];
      } else {
        this.form.categoryId = "";
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
    selectDateRange(v) {
      if (v) {
        this.searchForm.startDate = v[0];
        this.searchForm.endDate = v[1];
      }
    },
    changeTableSize(v) {
      this.tableSize = v;
    },
    getDataList() {
      // 多条件搜索获取表格数据
      this.loading = true;
      // this.getRequest("请求路径", this.searchForm).then(res => {
      //   this.loading = false;
      //   if (res.success) {
      //     this.data = res.result.content;
      //     this.total = res.result.totalElements;
      //     if (this.data.length == 0 && this.searchForm.pageNumber > 1) {
      //       this.searchForm.pageNumber -= 1;
      //       this.getDataList();
      //     }
      //   }
      // });
      // 以下为模拟数据
      this.data = [
        {
          id: "1",
          name: "bigdata",
          avatar: "https://s1.ax1x.com/2018/05/19/CcdVQP.png",
          categoryTitle: "分类1",
          categoryId: 1,
          type: 0,
          status: 0,
          createTime: "2018-08-08 00:08:00",
        },
        {
          id: "2",
          name: "bigdata",
          avatar: "https://s1.ax1x.com/2018/05/19/CcdVQP.png",
          categoryTitle: "分类2",
          categoryId: 2,
          type: 1,
          status: -1,
          createTime: "2018-08-08 00:08:00",
        },
      ];
      this.exportData = this.data;
      this.total = this.data.length;
      this.loading = false;
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
      this.selectDate = null;
      this.searchForm.startDate = "";
      this.searchForm.endDate = "";
      this.selectCat = [];
      this.searchForm.categoryId = "";
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
    handleDropdown(name) {
      if (name == "exportData") {
        if (this.selectList.length <= 0) {
          this.$Message.warning("您还未选择要导出的数据");
          return;
        }
        this.$Modal.confirm({
          title: "确认导出",
          content: "您确认要导出所选 " + this.selectList.length + " 条数据?",
          onOk: () => {
            this.$refs.exportTable.exportCsv({
              filename: "导出数据",
            });
          },
        });
      } else if (name == "exportAll") {
        this.modalExportAll = true;
      }
    },
    exportAll() {
      // this.getRequest("请求获取全部数据接口路径").then(res => {
      //   this.modalExportAll = false;
      //   if (res.success) {
      //     this.exportData = res.result;
      //     setTimeout(() => {
      //       this.$refs.exportTable.exportCsv({
      //         filename: "用户全部数据"
      //       });
      //     }, 1000);
      //   }
      // });
      // 模拟成功
      this.modalExportAll = false;
      this.$refs.exportTable.exportCsv({
        filename: "用户全部数据",
      });
    },
    handleCancel() {
      this.modalVisible = false;
    },
    handleSubmit() {
      this.$refs.form.validate((valid) => {
        if (valid) {
          if (this.modalType == 0) {
            // 添加 避免编辑后传入id等数据 记得删除
            delete this.form.id;
            delete this.form.status;
            if (this.form.password == "" || this.form.password == undefined) {
              this.errorPass = "请输入密码";
              return;
            }
            if (this.form.password.length < 6) {
              this.errorPass = "密码长度不得少于6位";
              return;
            }
            this.submitLoading = true;
            // this.postRequest("请求路径", this.form).then(res => {
            //   this.submitLoading = false;
            //   if (res.success) {
            //     this.$Message.success("操作成功");
            //     this.getDataList();
            //     this.modalVisible = false;
            //   }
            // });
            // 模拟成功
            this.submitLoading = false;
            this.$Message.success("操作成功");
            this.modalVisible = false;
          } else if (this.modalType == 1) {
            // 编辑
            this.submitLoading = true;
            // this.postRequest("请求路径", this.form).then(res => {
            //   this.submitLoading = false;
            //   if (res.success) {
            //     this.$Message.success("操作成功");
            //     this.getDataList();
            //     this.modalVisible = false;
            //   }
            // });
            // 模拟成功
            this.submitLoading = false;
            this.$Message.success("操作成功");
            this.modalVisible = false;
          }
        }
      });
    },
    handleUpload(v) {
      this.form.avatar = v;
    },
    add() {
      this.modalType = 0;
      this.modalTitle = "添加";
      this.$refs.form.resetFields();
      this.modalVisible = true;
    },
    edit(v) {
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
      this.form = data;
      this.modalVisible = true;
    },
    enable(v) {
      this.$Modal.confirm({
        title: "确认启用",
        // 记得确认修改此处
        content: "您确认要启用 " + v.name + " ?",
        loading: true,
        onOk: () => {
          // this.postRequest("请求路径/" + v.id).then(res => {
          //   this.$Modal.remove();
          //   if (res.success) {
          //     this.$Message.success("操作成功");
          //     this.getDataList();
          //   }
          // });
          //模拟成功
          this.$Message.success("操作成功");
          this.$Modal.remove();
          this.getDataList();
        },
      });
    },
    disable(v) {
      this.$Modal.confirm({
        title: "确认禁用",
        // 记得确认修改此处
        content: "您确认要禁用 " + v.name + " ?",
        loading: true,
        onOk: () => {
          // this.postRequest("请求路径/" + v.id).then(res => {
          //   this.$Modal.remove();
          //   if (res.success) {
          //     this.$Message.success("操作成功");
          //     this.getDataList();
          //   }
          // });
          //模拟成功
          this.$Message.success("操作成功");
          this.$Modal.remove();
          this.getDataList();
        },
      });
    },
    remove(v) {
      this.$Modal.confirm({
        title: "确认删除",
        // 记得确认修改此处
        content: "您确认要删除 " + v.name + " ?",
        loading: true,
        onOk: () => {
          // 删除
          // this.deleteRequest("请求地址，如/deleteByIds/" + v.id).then(res => {
          //   this.$Modal.remove();
          //   if (res.success) {
          //     this.$Message.success("操作成功");
          //     this.clearSelectAll();
          //     this.getDataList();
          //   }
          // });
          // 模拟请求成功
          this.$Message.success("操作成功");
          this.clearSelectAll();
          this.$Modal.remove();
          this.getDataList();
        },
      });
    },
    dropDown() {
      if (this.drop) {
        this.dropDownContent = "展开";
        this.dropDownIcon = "ios-arrow-down";
      } else {
        this.dropDownContent = "收起";
        this.dropDownIcon = "ios-arrow-up";
      }
      this.drop = !this.drop;
    },
    showSelect(e) {
      this.exportData = e;
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
          // 批量删除
          // this.deleteRequest("请求地址，如/deleteByIds/" + ids).then(res => {
          //   this.$Modal.remove();
          //   if (res.success) {
          //     this.$Message.success("操作成功");
          //     this.clearSelectAll();
          //     this.getDataList();
          //   }
          // });
          // 模拟请求成功
          this.$Message.success("操作成功");
          this.$Modal.remove();
          this.clearSelectAll();
          this.getDataList();
        },
      });
    },
  },
  mounted() {
    this.init();
  },
};
</script>