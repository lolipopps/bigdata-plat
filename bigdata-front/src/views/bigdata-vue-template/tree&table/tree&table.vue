<style lang="less">
@import "@/styles/tree&table-common.less";
</style>
<template>
  <div class="search">
    <Card>
      <Row type="flex" justify="space-between">
        <Col v-show="expand" span="5">
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
            placeholder="输入节点名搜索"
            clearable
          />
          <div style="position: relative">
            <div class="tree-bar" :style="{ maxHeight: maxHeight }">
              <Tree
                ref="tree"
                :data="treeData"
                :load-data="loadData"
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
          <Row v-show="openSearch">
            <Form ref="searchForm" :model="searchForm" inline :label-width="50">
              <FormItem label="名称" prop="name">
                <Input
                  type="text"
                  v-model="searchForm.name"
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
          <Row class="operation">
            <Button @click="add" type="primary" icon="md-add">添加</Button>
            <Button @click="delAll" icon="md-trash">批量删除</Button>
            <Button @click="getDataList" icon="md-refresh">刷新</Button>
            <Button type="dashed" @click="openSearch = !openSearch">{{
              openSearch ? "关闭搜索" : "开启搜索"
            }}</Button>
            <Button type="dashed" @click="openTip = !openTip">{{
              openTip ? "关闭提示" : "开启提示"
            }}</Button>
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

    <Modal
      :title="modalTitle"
      v-model="modalVisible"
      :mask-closable="false"
      :width="500"
    >
      <Form ref="form" :model="form" :label-width="70" :rules="formValidate">
        <FormItem label="名称" prop="name">
          <Input v-model="form.name" />
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
export default {
  name: "treeAndTable",
  data() {
    return {
      openSearch: true, // 显示搜索
      openTip: true, // 显示提示
      treeLoading: false, // 树加载状态
      maxHeight: "500px",
      loading: false, // 表加载状态
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
        sort: "createTime", // 默认排序字段
        order: "desc", // 默认排序方式
      },
      modalType: 0, // 添加或编辑标识
      modalVisible: false, // 添加或编辑显示
      modalTitle: "", // 添加或编辑标题
      form: {
        // 添加或编辑表单对象初始化数据
        name: "",
        sex: 1,
        type: 0,
      },
      formValidate: {
        // 表单验证规则
        name: [{ required: true, message: "请输入", trigger: "blur" }],
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
          key: "name",
          minWidth: 200,
          sortable: true,
        },
        {
          title: "描述",
          key: "description",
          minWidth: 200,
          sortable: true,
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
        },
        {
          title: "创建时间",
          key: "createTime",
          width: 200,
          sortable: true,
          sortType: "desc",
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
      // 初始化一级节点
      this.getParentList();
      // 获取表单数据
      this.getDataList();
    },
    getParentList() {
      // this.treeLoading = true;
      // this.getRequest("一级数据请求路径，如/tree/getByParentId/0").then(res => {
      //   this.treeLoading = false;
      //   if (res.success) {
      //     res.result.forEach(function(e) {
      //       if (e.isParent) {
      //         e.loading = false;
      //         e.children = [];
      //       }
      //     });
      //     this.treeData = res.result;
      //   }
      // });
      // 模拟请求成功
      this.treeData = [
        {
          title: "一级1",
          id: "1",
          parentId: "0",
          parentTitle: "一级节点",
          status: 0,
          loading: false,
          children: [
            {
              title: "二级1",
              id: "2",
              parentId: "1",
              status: 0,
              parentTitle: "一级1",
            },
          ],
        },
        {
          title: "一级2",
          id: "3",
          parentId: "0",
          parentTitle: "一级节点",
          status: 0,
        },
      ];
    },
    loadData(item, callback) {
      // 异步加载树子节点数据
      // this.getRequest("请求路径，如/tree/getByParentId/" + item.id).then(res => {
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
    search() {
      // 搜索树
      if (this.searchKey) {
        // 模拟请求
        // this.treeLoading = true;
        // this.getRequest("搜索请求路径", { title: this.searchKey }).then(res => {
        //   this.treeLoading = false;
        //   if (res.success) {
        //     this.treeData = res.result;
        //   }
        // });
        // 模拟请求成功
        this.treeData = [
          {
            title: "这里需要请求后端接口",
            id: "1",
            parentId: "0",
            parentTitle: "一级节点",
            status: 0,
          },
          {
            title: "所以这里是假数据",
            id: "4",
            parentId: "0",
            parentTitle: "一级节点",
            status: 0,
          },
          {
            title: "我是被禁用的节点",
            id: "5",
            parentId: "0",
            parentTitle: "一级节点",
            status: -1,
          },
        ];
      } else {
        // 为空重新加载
        this.getParentList();
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
        this.editTitle = data.title;
        // 重新加载表
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
      // 根据用户选择树加载表数据
      this.searchForm.selectId = this.selectNode.id;
      // 带多条件搜索参数获取表单数据 请自行修改接口
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
      // 模拟表格变化 模拟数据
      if (!this.selectNode.id) {
        // id为null获取全部数据
        this.data = [
          {
            id: "1",
            name: "bigdata",
            description: "我是一级1的数据",
            status: 0,
            createTime: "2018-08-08 00:08:00",
          },
          {
            id: "2",
            name: "bigdata",
            description: "我是二级1的数据",
            status: 0,
            createTime: "2018-08-08 00:08:00",
          },
          {
            id: "3",
            name: "bigdata",
            description: "我是一级2的数据",
            status: -1,
            createTime: "2018-08-08 00:08:00",
          },
        ];
      } else if (this.selectNode.id == "1") {
        this.data = [
          {
            id: "1",
            name: "bigdata",
            description: "我是一级1的数据",
            status: 0,
            createTime: "2018-08-08 00:08:00",
          },
        ];
      } else if (this.selectNode.id == "2") {
        this.data = [
          {
            id: "2",
            name: "bigdata",
            description: "我是二级1的数据",
            status: 0,
            createTime: "2018-08-08 00:08:00",
          },
        ];
      } else if (this.selectNode.id == "3") {
        this.data = [
          {
            id: "3",
            name: "Present By bigdata",
            description: "我是一级2的数据",
            status: -1,
            createTime: "2018-08-08 00:08:00",
          },
        ];
      }
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
    handelSubmit() {
      this.$refs.form.validate((valid) => {
        if (valid) {
          this.submitLoading = true;
          if (this.modalType == 0) {
            // 添加 避免编辑后传入id等数据 记得删除
            delete this.form.id;
            delete this.form.status;
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
    // 计算高度
    let height = document.documentElement.clientHeight;
    this.maxHeight = Number(height - 287) + "px";
    this.init();
  },
};
</script>