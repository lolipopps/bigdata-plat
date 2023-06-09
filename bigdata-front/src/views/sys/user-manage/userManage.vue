<style lang="less">
@import "@/styles/table-common.less";
@import "./userManage.less";
</style>
<template>
  <div class="search">
    <Card>
      <Row v-show="openSearch" @keydown.enter.native="handleSearch">
        <Form ref="searchForm" :model="searchForm" inline :label-width="70">
          <FormItem label="用户名" prop="nickname">
            <Input
              type="text"
              v-model="searchForm.nickname"
              clearable
              placeholder="请输入用户名"
              style="width: 200px"
            />
          </FormItem>
          <FormItem label="部门" prop="department">
            <department-choose
              @on-change="handleSelectDep"
              style="width: 200px"
              ref="dep"
            ></department-choose>
          </FormItem>
          <span v-if="drop">
            <FormItem label="手机号" prop="mobile">
              <Input
                type="text"
                v-model="searchForm.mobile"
                clearable
                placeholder="请输入手机号"
                style="width: 200px"
              />
            </FormItem>
            <FormItem label="邮箱" prop="email">
              <Input
                type="text"
                v-model="searchForm.email"
                clearable
                placeholder="请输入邮箱"
                style="width: 200px"
              />
            </FormItem>
            <FormItem label="性别" prop="sex">
              <dict dict="sex" v-model="searchForm.sex" style="width: 200px" />
            </FormItem>
            <FormItem label="登录账号" prop="username">
              <Input
                type="text"
                v-model="searchForm.username"
                clearable
                placeholder="请输入登录账号"
                style="width: 200px"
              />
            </FormItem>
            <FormItem label="用户ID" prop="id">
              <Input
                type="text"
                v-model="searchForm.id"
                clearable
                placeholder="请输入用户ID"
                style="width: 200px"
              />
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
          <Button @click="add" type="primary" icon="md-add">添加用户</Button>
          <Button @click="delAll" icon="md-trash">批量删除</Button>
          <Dropdown @on-click="handleDropdown">
            <Button>
              更多操作
              <Icon type="md-arrow-dropdown" />
            </Button>
            <DropdownMenu slot="list">
              <DropdownItem name="reset">重置用户密码</DropdownItem>
              <DropdownItem name="exportData">导出所选数据</DropdownItem>
              <DropdownItem name="exportAll">导出全部数据</DropdownItem>
              <DropdownItem name="importData">导入数据(付费)</DropdownItem>
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

    <!-- 自定义导出数据 -->
    <Modal
      v-model="exportModalVisible"
      :title="exportTitle"
      :loading="loadingExport"
      @on-ok="exportCustomData"
    >
      <Form ref="exportForm" :label-width="100">
        <FormItem label="导出文件名">
          <Input v-model="filename" />
        </FormItem>
        <FormItem label="自定义导出列">
          <CheckboxGroup v-model="chooseColumns">
            <Checkbox
              v-for="(item, i) in exportColumns"
              :label="item.title"
              :key="i"
              :value="item.checked"
              :disabled="item.disabled"
            ></Checkbox>
          </CheckboxGroup>
        </FormItem>
      </Form>
    </Modal>
    <!-- 导入数据 -->
    <Drawer
      title="导入数据"
      closable
      v-model="importModalVisible"
      width="800"
      draggable
    >
      <div
        style="
          display: flex;
          justify-content: space-between;
          align-items: center;
        "
      >
        <Upload action :before-upload="beforeUploadImport" accept=".xls, .xlsx">
          <Button
            :loading="reading"
            icon="ios-cloud-upload-outline"
            style="margin-right: 10px"
            >上传Excel文件</Button
          >
          <span v-if="uploadfile.name"
            >当前选择文件：{{ uploadfile.name }}</span
          >
        </Upload>
        <Button @click="clearImportData" icon="md-trash">清空数据</Button>
      </div>
      <Alert type="warning" show-icon
        >导入前请下载查看导入模版数据文件查看所需字段及说明，确保数据格式正确，不得修改列英文名称</Alert
      >
      <Table
        :columns="importColumns"
        border
        :height="height"
        :data="importTableData"
        ref="importTable"
      ></Table>
      <div class="drawer-footer">
        <Button @click="downloadTemple" type="info">下载导入模板</Button>
        <div style="position: absolute; right: 15px; display: inline-block">
          <Button @click="importModalVisible = false">关闭</Button>
          <Button
            :loading="importLoading"
            :disabled="importTableData.length <= 0"
            @click="importData"
            style="margin-left: 8px"
            type="primary"
          >
            确认导入
            <span v-if="importTableData.length > 0"
              >{{ importTableData.length }} 条数据</span
            >
          </Button>
        </div>
      </div>
    </Drawer>

    <check-password ref="checkPass" @on-success="resetPass" />

    <addEdit
      :data="form"
      :type="showType"
      v-model="showUser"
      @on-submit="getDataList"
    />
  </div>
</template>

<script>
import {
  getUserListData,
  enableUser,
  disableUser,
  deleteUser,
  getAllUserData,
  importUserData,
  resetUserPass,
} from "@/api/index";
import departmentChoose from "@/views/my-components/bigdata/department-choose";
import checkPassword from "@/views/my-components/bigdata/check-password";
import { shortcuts } from "@/libs/shortcuts";
// 模版导入文件表数据
import { userColumns, userData } from "./importTemplate";
// 指定导出列数据
import { exportColumn } from "./exportColumn";
import excel from "@/libs/excel";
import addEdit from "./addEdit.vue";
import dict from "@/views/my-components/bigdata/dict";
export default {
  name: "user-manage",
  components: {
    departmentChoose,
    checkPassword,
    addEdit,
    dict,
  },
  data() {
    return {
      tableSize: "default",
      height: 510,
      showUser: false,
      showType: "0",
      openSearch: true,
      openTip: true,
      loading: true,
      reading: false,
      importLoading: false,
      loadingExport: true,
      exportModalVisible: false,
      importModalVisible: false,
      drop: false,
      dropDownContent: "展开",
      dropDownIcon: "ios-arrow-down",
      selectList: [],
      searchForm: {
        id: "",
        nickname: "",
        username: "",
        departmentId: "",
        mobile: "",
        email: "",
        sex: "",
        type: "",
        status: "",
        pageNumber: 1,
        pageSize: 10,
        sort: "createTime",
        order: "desc",
        startDate: "",
        endDate: "",
      },
      selectDate: null,
      options: {
        shortcuts: shortcuts,
      },
      form: {},
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
          title: "登录账号",
          key: "username",
          minWidth: 125,
          sortable: true,
          fixed: "left",
        },
        {
          title: "用户名",
          key: "nickname",
          minWidth: 125,
          sortable: true,
          fixed: "left",
          render: (h, params) => {
            return h(
              "a",
              {
                on: {
                  click: () => {
                    this.showDetail(params.row);
                  },
                },
              },
              params.row.nickname
            );
          },
        },
        {
          title: "头像",
          key: "avatar",
          width: 80,
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
          title: "所属部门",
          key: "departmentTitle",
          minWidth: 120,
        },
        {
          title: "手机",
          key: "mobile",
          minWidth: 125,
          sortable: true,
        },
        {
          title: "邮箱",
          key: "email",
          minWidth: 180,
          sortable: true,
        },
        {
          title: "性别",
          key: "sex",
          width: 70,
          align: "center",
        },
        {
          title: "类型",
          key: "type",
          align: "center",
          width: 110,
          render: (h, params) => {
            let re = "",
              color = "";
            if (params.row.type == 0) {
              re = "普通用户";
              color = "default";
            } else if (params.row.type == 1) {
              re = "管理员";
              color = "blue";
            }
            return h("div", [
              h(
                "Tag",
                {
                  props: {
                    color: color,
                  },
                },
                re
              ),
            ]);
          },
          filters: [
            {
              label: "普通用户",
              value: 0,
            },
            {
              label: "管理员",
              value: 1,
            },
          ],
          filterMultiple: false,
          filterRemote: (e) => {
            let v = "";
            if (e.length > 0) {
              v = e[0];
            }
            this.searchForm.type = v;
            this.searchForm.pageNumber = 1;
            this.getDataList();
          },
        },
        {
          title: "状态",
          key: "status",
          align: "center",
          width: 100,
          render: (h, params) => {
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
          filterRemote: (e) => {
            let v = "";
            if (e.length > 0) {
              v = e[0];
            }
            this.searchForm.status = v;
            this.searchForm.pageNumber = 1;
            this.getDataList();
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
          width: 170,
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
      exportColumns: exportColumn,
      chooseColumns: [],
      filename: "用户数据",
      exportTitle: "确认导出",
      exportType: "",
      importTableData: [],
      importColumns: [],
      uploadfile: {
        name: "",
      },
      tempColumns: userColumns,
      tempData: userData,
      data: [],
      exportData: [],
      total: 0,
    };
  },
  methods: {
    init() {
      this.getDataList();
      // 初始化导出列数据
      let array = [];
      this.exportColumns.forEach((e) => {
        // 指定列限制权限
        if (
          !this.getStore("roles").includes("ROLE_ADMIN") &&
          e.key == "mobile"
        ) {
          e.title = "[无权导出] " + e.title;
          e.disabled = true;
        } else {
          e.disabled = false;
        }
        array.push(e.title);
      });
      this.chooseColumns = array;
    },
    handleSelectDep(v) {
      this.searchForm.departmentId = v;
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
      // 多条件搜索用户列表
      this.loading = true;
      getUserListData(this.searchForm).then((res) => {
        this.loading = false;
        if (res.success) {
          if (!this.getStore("roles").includes("ROLE_ADMIN")) {
            res.result.content.forEach((e) => {
              e.mobile = "您无权查看该数据";
            });
          }
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
      this.selectDate = null;
      this.searchForm.startDate = "";
      this.searchForm.endDate = "";
      this.$refs.dep.clearSelect();
      this.searchForm.departmentId = "";
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
      if (name == "reset") {
        if (this.selectList.length <= 0) {
          this.$Message.warning("您还未选择要重置密码的用户");
          return;
        }
        this.$refs.checkPass.show();
      } else if (name == "exportData") {
        if (this.selectList.length <= 0) {
          this.$Message.warning("您还未选择要导出的数据");
          return;
        }
        this.exportType = "part";
        this.exportModalVisible = true;
        this.exportTitle =
          "确认导出 " + this.selectList.length + " 条数据(付费)";
      } else if (name == "exportAll") {
        this.exportType = "all";
        this.exportModalVisible = true;
        this.exportTitle = "确认导出全部 " + this.total + " 条数据(付费)";
        getAllUserData().then((res) => {
          if (res.success) {
            this.exportData = res.result;
          }
        });
      } else if (name == "importData") {
        this.importModalVisible = true;
      }
    },
    resetPass() {
      this.$Modal.confirm({
        title: "确认重置",
        content:
          "您确认要重置所选的 " +
          this.selectList.length +
          " 条用户数据密码为 123456 ?",
        loading: true,
        onOk: () => {
          let ids = "";
          this.selectList.forEach(function (e) {
            ids += e.id + ",";
          });
          ids = ids.substring(0, ids.length - 1);
          resetUserPass({ ids: ids }).then((res) => {
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
    exportCustomData() {
      if (this.filename == "") {
        this.filename = "用户数据";
      }
      // 判断勾选导出列
      let exportColumns = [];
      this.exportColumns.forEach((e) => {
        this.chooseColumns.forEach((c) => {
          if (e.title == c && !e.disabled) {
            exportColumns.push(e);
          }
        });
      });
      this.exportModalVisible = false;
      let title = [];
      let key = [];
      exportColumns.forEach((e) => {
        title.push(e.title);
        key.push(e.key);
      });
      const params = {
        title: title,
        key: key,
        data: this.exportData,
        autoWidth: true,
        filename: this.filename,
      };
      excel.export_array_to_excel(params);
    },
    beforeUploadImport(file) {
      this.uploadfile = file;
      const fileExt = file.name.split(".").pop().toLocaleLowerCase();
      if (fileExt == "xlsx" || fileExt == "xls") {
        this.readFile(file);
        this.file = file;
      } else {
        this.$Notice.warning({
          title: "文件类型错误",
          desc:
            "所选文件‘ " +
            file.name +
            " ’不是EXCEL文件，请选择后缀为.xlsx或者.xls的EXCEL文件。",
        });
      }
      return false;
    },
    // 读取文件
    readFile(file) {
      this.reading = true;
      const reader = new FileReader();
      reader.readAsArrayBuffer(file);
      reader.onerror = (e) => {
        this.reading = false;
        this.$Message.error("文件读取出错");
      };
      reader.onload = (e) => {
        const data = e.target.result;
        const { header, results } = excel.read(data, "array");
        const tableTitle = header.map((item) => {
          return { title: item, key: item, minWidth: 130, align: "center" };
        });
        this.importTableData = results;
        this.importColumns = tableTitle;
        this.reading = false;
        this.$Message.success("读取数据成功");
      };
    },
    clearImportData() {
      this.importTableData = [];
      this.importColumns = [];
      this.uploadfile = {};
    },
    downloadTemple() {
      let title = [];
      let key = [];
      userColumns.forEach((e) => {
        title.push(e.title);
        key.push(e.key);
      });
      const params = {
        title: title,
        key: key,
        data: userData,
        autoWidth: true,
        filename: "导入用户数据模版",
      };
      excel.export_array_to_excel(params);
    },
    importData() {
      this.importLoading = true;
      importUserData(this.importTableData).then((res) => {
        this.importLoading = false;
        if (res.success) {
          this.importModalVisible = false;
          this.getDataList();
          this.$Modal.info({
            title: "导入结果",
            content: res.message,
          });
        }
      });
    },
    showDetail(v) {
      // 转换null为""
      for (let attr in v) {
        if (v[attr] == null) {
          v[attr] = "";
        }
      }
      let str = JSON.stringify(v);
      let data = JSON.parse(str);
      this.form = data;
      this.showType = "0";
      this.showUser = true;
    },
    add() {
      this.showType = "2";
      this.showUser = true;
    },
    edit(v) {
      // 转换null为""
      for (let attr in v) {
        if (v[attr] == null) {
          v[attr] = "";
        }
      }
      let str = JSON.stringify(v);
      let data = JSON.parse(str);
      this.form = data;
      this.showType = "1";
      this.showUser = true;
    },
    enable(v) {
      this.$Modal.confirm({
        title: "确认启用",
        content: "您确认要启用用户 " + v.username + " ?",
        loading: true,
        onOk: () => {
          enableUser(v.id).then((res) => {
            this.$Modal.remove();
            if (res.success) {
              this.$Message.success("操作成功");
              this.getDataList();
            }
          });
        },
      });
    },
    disable(v) {
      this.$Modal.confirm({
        title: "确认禁用",
        content: "您确认要禁用用户 " + v.username + " ?",
        loading: true,
        onOk: () => {
          disableUser(v.id).then((res) => {
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
        content: "您确认要删除用户 " + v.username + " ?",
        loading: true,
        onOk: () => {
          deleteUser({ ids: v.id }).then((res) => {
            this.$Modal.remove();
            if (res.success) {
              this.clearSelectAll();
              this.$Message.success("删除成功");
              this.getDataList();
            }
          });
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
          deleteUser({ ids: ids }).then((res) => {
            this.$Modal.remove();
            if (res.success) {
              this.$Message.success("删除成功");
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
    this.height = Number(document.documentElement.clientHeight - 230);
    this.init();
  },
};
</script>