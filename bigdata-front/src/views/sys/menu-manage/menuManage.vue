<style lang="less">
@import "@/styles/tree-common.less";
@import "./menuManage.less";
</style>
<template>
  <div class="search">
    <Card>
      <Row class="operation" align="middle" justify="space-between">
        <div>
          <Button
            @click="add"
            type="primary"
            icon="md-add"
            v-show="showType == 'tree'"
            >添加子节点</Button
          >
          <Button @click="addRoot" icon="md-add">添加顶部菜单</Button>
          <Button @click="delAll" icon="md-trash">批量删除</Button>
          <Button
            @click="getParentList"
            icon="md-refresh"
            v-show="showType == 'list'"
            >刷新</Button
          >
          <Dropdown @on-click="handleDropdown" v-show="showType == 'tree'">
            <Button>
              更多操作
              <Icon type="md-arrow-dropdown"></Icon>
            </Button>
            <DropdownMenu slot="list">
              <DropdownItem name="refresh">刷新</DropdownItem>
              <DropdownItem name="expandOne">收合所有</DropdownItem>
              <DropdownItem name="expandTwo">仅展开一级</DropdownItem>
              <DropdownItem name="expandThree">仅展开两级</DropdownItem>
              <DropdownItem name="expandAll">展开所有</DropdownItem>
            </DropdownMenu>
          </Dropdown>
          <Input
            v-model="searchKey"
            suffix="ios-search"
            @on-change="search"
            placeholder="输入菜单名搜索"
            clearable
            style="width: 250px"
            v-show="showType == 'list'"
          />
          <i-switch
            v-model="strict"
            size="large"
            style="margin-left: 5px"
            v-show="showType == 'tree'"
          >
            <span slot="open">级联</span>
            <span slot="close">单选</span>
          </i-switch>
        </div>
        <div>
          <RadioGroup v-model="showType" type="button">
            <Radio title="树结构" label="tree">
              <Icon type="md-list"></Icon>
            </Radio>
            <Radio title="列表" label="list">
              <Icon type="ios-apps"></Icon>
            </Radio>
          </RadioGroup>
        </div>
      </Row>
      <Alert show-icon v-show="showType == 'list'">
        已选择
        <span class="select-count">{{ selectList.length }}</span> 项
        <a class="select-clear" @click="clearSelectAll">清空</a>
      </Alert>
      <Table
        row-key="title"
        :load-data="loadData"
        :columns="columns"
        :data="tableData"
        :loading="loading"
        border
        :update-show-children="true"
        ref="table"
        @on-selection-change="showSelect"
        v-if="showType == 'list'"
      ></Table>
      <Row type="flex" justify="start" :gutter="16" v-show="showType == 'tree'">
        <Col :sm="8" :md="8" :lg="8" :xl="6">
          <Alert show-icon>
            当前选择编辑：
            <span class="select-title">{{ editTitle }}</span>
            <a
              class="select-clear"
              v-show="form.id && editTitle"
              @click="cancelEdit"
              >取消选择</a
            >
          </Alert>
          <Input
            v-model="searchKey"
            suffix="ios-search"
            @on-change="search"
            placeholder="输入菜单名搜索"
            clearable
          />
          <div style="position: relative">
            <div class="tree-bar" :style="{ maxHeight: maxHeight }">
              <Tree
                ref="tree"
                :data="data"
                show-checkbox
                :render="renderContent"
                @on-select-change="selectTree"
                @on-check-change="changeSelect"
                :check-strictly="!strict"
              ></Tree>
            </div>
            <Spin size="large" fix v-if="loading"></Spin>
          </div>
        </Col>
        <Col :sm="16" :md="16" :lg="16" :xl="9">
          <Form ref="form" :model="form" :label-width="110">
            <FormItem label="类型" prop="type">
              <div v-show="form.type == -1">
                <Icon
                  type="ios-navigate-outline"
                  size="16"
                  style="margin-right: 5px"
                ></Icon>
                <span>顶部菜单</span>
              </div>
              <div v-show="form.type == 0">
                <Icon
                  type="ios-list-box-outline"
                  size="16"
                  style="margin-right: 5px"
                ></Icon>
                <span>页面菜单</span>
              </div>
              <div v-show="form.type == 1">
                <Icon
                  type="md-radio-button-on"
                  size="16"
                  style="margin-right: 5px"
                ></Icon>
                <span>操作按钮</span>
              </div>
            </FormItem>
            <FormItem label="上级菜单" prop="parentTitle" class="form-noheight">
              <div style="display: flex">
                <Input
                  v-model="form.parentTitle"
                  readonly
                  style="margin-right: 10px"
                />
                <Poptip
                  transfer
                  trigger="click"
                  placement="right-start"
                  title="选择上级菜单"
                  width="250"
                >
                  <Button icon="md-list">选择菜单</Button>
                  <div slot="content" class="tree-bar tree-select">
                    <Tree
                      :data="dataEdit"
                      @on-select-change="selectTreeEdit"
                    ></Tree>
                    <Spin size="large" fix v-if="loading"></Spin>
                  </div>
                </Poptip>
              </div>
            </FormItem>
            <FormItem
              label="名称"
              prop="title"
              :rules="{
                required: true,
                message: '请输入名称',
                trigger: 'blur',
              }"
              v-if="form.type == -1 || form.type == 0"
            >
              <div style="display: flex">
                <div style="width: 100%">
                  <Input v-model="form.title">
                    <Poptip
                      transfer
                      trigger="hover"
                      title="多语言"
                      placement="right"
                      width="350"
                      style="width: 17px; cursor: pointer"
                      slot="append"
                    >
                      <Button icon="ios-globe-outline"></Button>
                      <div slot="content">
                        <Form :label-width="80">
                          <FormItem label="启用多语言">
                            <i-switch v-model="form.localize" size="large">
                              <span slot="open">开</span>
                              <span slot="close">关</span>
                            </i-switch>
                          </FormItem>
                          <FormItem label="多语言Key">
                            <Input
                              v-model="form.i18n"
                              placeholder="输入用于渲染I18n的$t('KEY')"
                            ></Input>
                          </FormItem>
                        </Form>
                      </div>
                    </Poptip>
                  </Input>
                </div>
              </div>
            </FormItem>
            <FormItem
              label="名称"
              prop="title"
              :rules="{
                required: true,
                message: '请输入名称',
                trigger: 'blur',
              }"
              v-if="form.type == 1"
              class="block-tool"
            >
              <Tooltip placement="right" content="操作按钮名称不得重复">
                <Input v-model="form.title" />
              </Tooltip>
            </FormItem>
            <FormItem
              label="路径"
              prop="path"
              :rules="{
                required: true,
                message: '请输入路径',
                trigger: 'blur',
              }"
              v-if="form.type == 0"
            >
              <Input v-model="form.path" />
            </FormItem>
            <FormItem
              label="请求路径"
              prop="path"
              v-if="form.type == 1"
              class="block-tool"
              :required="false"
            >
              <Tooltip
                placement="right"
                :max-width="230"
                transfer
                content="填写后端请求URL，后端将作权限拦截，若无可留空"
              >
                <Input v-model="form.path" />
              </Tooltip>
            </FormItem>
            <FormItem
              label="按钮权限类型"
              prop="buttonType"
              v-if="form.type == 1"
            >
              <dict
                dict="permission_type"
                v-model="form.buttonType"
                placeholder="请选择或输入搜索"
                filterable
                clearable
              />
            </FormItem>
            <FormItem
              label="英文名"
              prop="name"
              :rules="{
                required: true,
                message: '请输入英文名',
                trigger: 'blur',
              }"
              v-if="form.type == -1"
              class="block-tool"
            >
              <Tooltip placement="right" content="需唯一">
                <Input v-model="form.name" />
              </Tooltip>
            </FormItem>
            <FormItem
              label="路由英文名"
              prop="name"
              :rules="{
                required: true,
                message: '请输入路由英文名',
                trigger: 'blur',
              }"
              v-if="form.type == 0"
              class="block-tool"
            >
              <Tooltip placement="right" content="需唯一" transfer>
                <Input v-model="form.name" />
              </Tooltip>
            </FormItem>
            <FormItem
              label="图标"
              prop="icon"
              :rules="{
                required: true,
                message: '请输入图标',
                trigger: 'blur',
              }"
              v-if="form.type == -1 || form.type == 0"
            >
              <icon-choose
                showCustom
                showInput
                v-model="form.icon"
              ></icon-choose>
            </FormItem>
            <FormItem label="显示红点" prop="component" v-if="form.type == -1">
              <i-switch
                size="large"
                v-model="form.component"
                :true-value="'hot'"
                :false-value="'normal'"
              >
                <span slot="open">开</span>
                <span slot="close">关</span>
              </i-switch>
            </FormItem>
            <FormItem label="系统站内菜单" prop="isMenu" v-if="form.type == -1">
              <i-switch size="large" v-model="form.isMenu">
                <span slot="open">是</span>
                <span slot="close">否</span>
              </i-switch>
            </FormItem>
            <FormItem
              label="其他链接"
              prop="url"
              :rules="{
                required: true,
                message: '请输入其他链接',
                trigger: 'blur',
              }"
              v-if="form.type == -1 && !form.isMenu"
            >
              <Input v-model="form.url" placeholder="Http链接或路由名" />
            </FormItem>
            <FormItem
              label="打开方式"
              prop="description"
              :rules="{
                required: true,
                message: '请选择打开方式',
                trigger: 'change',
              }"
              v-if="form.type == -1 && !form.isMenu"
            >
              <Select v-model="form.description" placeholder="请选择">
                <Option value="direct" label="直接跳转">
                  <span style="margin-right: 10px">直接跳转</span>
                  <span style="color: #ccc">其他链接填写完整链接</span>
                </Option>
                <Option value="window" label="新窗口打开">
                  <span style="margin-right: 10px">新窗口打开</span>
                  <span style="color: #ccc">其他链接填写完整链接</span>
                </Option>
                <Option value="route" label="站内路由">
                  <span style="margin-right: 10px">站内路由</span>
                  <span style="color: #ccc">其他链接填写Vue路由名</span>
                </Option>
              </Select>
            </FormItem>
            <FormItem
              label="前端组件"
              prop="component"
              :rules="{
                required: true,
                message: '请输入前端组件',
                trigger: 'blur',
              }"
              v-if="form.type == 0"
            >
              <Input v-model="form.component" />
            </FormItem>
            <FormItem
              label="第三方链接"
              prop="url"
              v-if="form.type == 0 && form.level == 2"
              :required="false"
            >
              <Input
                v-model="form.url"
                placeholder="http://"
                @on-change="changeEditUrl"
              />
              <span class="url-remark"
                >前端组件需为 sys/monitor/monitor 时生效</span
              >
            </FormItem>
            <FormItem
              label="排序值"
              prop="sortOrder"
              :rules="{
                required: true,
                type: 'number',
                message: '请输入前端组件',
                trigger: 'blur',
              }"
            >
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
            <FormItem
              label="始终显示"
              prop="showAlways"
              v-if="form.level == 1"
              class="block-tool"
            >
              <i-switch size="large" v-model="form.showAlways">
                <span slot="open">是</span>
                <span slot="close">否</span>
              </i-switch>
              <Tooltip
                content="当设为不始终显示时，一级菜单下仅有一个子级菜单只会显示该子级菜单，避免用户多次点击"
                placement="right"
                transfer
                max-width="280"
                style="display: inline-block !important"
              >
                <Icon
                  type="md-help-circle"
                  size="20"
                  color="#c5c5c5"
                  style="margin-left: 10px; cursor: pointer"
                />
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
            <FormItem>
              <Button
                style="margin-right: 5px"
                @click="submitEdit"
                :loading="submitLoading"
                :disabled="!form.id || !editTitle"
                type="primary"
                >修改并保存</Button
              >
              <Button @click="handleReset">重置</Button>
            </FormItem>
          </Form>
        </Col>
      </Row>
    </Card>

    <add
      :form="form"
      :isRoot="isRoot"
      :dataLength="data.length"
      v-model="showAdd"
      @on-submit="addSuccess"
    />
  </div>
</template>

<script>
import {
  loadPermission,
  editPermission,
  deletePermission,
  searchPermission,
} from "@/api/index";
import dict from "@/views/my-components/bigdata/dict";
import IconChoose from "@/views/my-components/bigdata/icon-choose";
import util from "@/libs/util.js";
import add from "./add.vue";
export default {
  name: "menu-manage",
  components: {
    dict,
    IconChoose,
    add,
  },
  data() {
    return {
      showType: "tree",
      columns: [
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
          title: "菜单名称",
          key: "title",
          minWidth: 150,
          sortable: true,
          tree: true,
        },
        {
          title: "英文名",
          key: "name",
          minWidth: 100,
          sortable: true,
        },
        {
          title: "类型",
          key: "type",
          minWidth: 120,
          sortable: true,
          align: "center",
          render: (h, params) => {
            let icon = "",
              type = "",
              level = params.row.level;
            if (level == 0) {
              icon = "ios-navigate";
              type = "顶部菜单";
            } else if (level == 1) {
              icon = "md-list-box";
              type = "页面菜单";
            } else if (level == 2) {
              icon = "md-list";
              type = "页面菜单";
            } else if (level == 3) {
              icon = "md-radio-button-on";
              type = "操作按钮";
            } else {
              icon = "md-radio-button-off";
            }
            return h("div", [
              h("Icon", {
                props: {
                  type: icon,
                },
                style: {
                  margin: "0 5px 0 0",
                },
              }),
              h("span", type),
            ]);
          },
        },
        {
          title: "图标",
          key: "icon",
          minWidth: 100,
          sortable: true,
          align: "center",
          render: (h, params) => {
            return h("div", [
              h("XIcon", {
                props: {
                  type: params.row.icon,
                },
              }),
            ]);
          },
        },
        {
          title: "路径/URL",
          minWidth: 100,
          key: "path",
          tooltip: true,
        },
        {
          title: "排序",
          key: "sortOrder",
          minWidth: 100,
          sortable: true,
          align: "center",
          sortType: "asc",
        },
        {
          title: "创建时间",
          key: "createTime",
          sortable: true,
          width: 170,
        },
        {
          title: "操作",
          key: "action",
          width: 200,
          fixed: "right",
          align: "center",
          render: (h, params) => {
            return h("div", [
              h(
                "a",
                {
                  on: {
                    click: () => {
                      this.tableAdd(params.row);
                    },
                  },
                },
                "添加子节点"
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
      loading: true,
      strict: true,
      maxHeight: "500px",
      expandLevel: 1,
      selectList: [],
      searchKey: "",
      parentTitle: "",
      isRoot: false,
      showAdd: false,
      editTitle: "",
      form: {
        id: "",
        title: "",
        name: "",
        icon: "",
        path: "",
        component: "",
        parentId: "",
        buttonType: "",
        isMenu: true,
        type: -1,
        sortOrder: 0,
        level: 0,
        status: 0,
        url: "",
        showAlways: true,
        parentTitle: "",
      },
      submitLoading: false,
      data: [],
      dataEdit: [],
      tableData: [],
    };
  },
  methods: {
    init() {
      this.getAllList();
      this.getParentList(null, true);
    },
    getParentList(f, notShowLoading) {
      if (!notShowLoading) {
        this.loading = true;
      }
      loadPermission("0").then((res) => {
        if (!notShowLoading) {
          this.loading = false;
        }
        if (res.success) {
          res.result.forEach(function (e) {
            if (e.isParent) {
              e.children = [];
              e._loading = false;
            }
          });
          this.tableData = res.result;
        }
      });
    },
    loadData(item, callback) {
      loadPermission(item.id).then((res) => {
        if (res.success) {
          res.result.forEach(function (e) {
            if (e.isParent) {
              e.children = [];
              e._loading = false;
            }
          });
          callback(res.result);
        }
      });
    },
    renderContent(h, { root, node, data }) {
      let icon = "md-radio-button-off";
      if (data.level == 0) {
        icon = "ios-navigate";
      } else if (data.level == 1) {
        icon = "md-list-box";
      } else if (data.level == 2) {
        icon = "md-list";
      } else if (data.level == 3) {
        icon = "md-radio-button-on";
      }
      return h("span", [
        h("span", [
          h("Icon", {
            props: {
              type: icon,
              size: "16",
            },
            style: {
              "margin-right": "8px",
              "margin-bottom": "3px",
            },
          }),
          h("span", data.title),
        ]),
      ]);
    },
    handleDropdown(name) {
      if (name == "expandOne") {
        this.expandLevel = 1;
      } else if (name == "expandTwo") {
        this.expandLevel = 2;
      } else if (name == "expandThree") {
        this.expandLevel = 3;
      } else if (name == "expandAll") {
        this.expandLevel = 4;
      }
      this.getAllList();
    },
    getAllList() {
      this.loading = true;
      this.getRequest("/permission/getAllList").then((res) => {
        this.loading = false;
        if (res.success) {
          // 仅展开指定级数 默认后端已展开所有
          let expandLevel = this.expandLevel;
          res.result.forEach(function (e) {
            if (expandLevel == 1) {
              if (e.level == 0) {
                e.expand = false;
              }
              if (e.children && e.children.length > 0) {
                e.children.forEach(function (c) {
                  if (c.level == 1) {
                    c.expand = false;
                  }
                  if (c.children && c.children.length > 0) {
                    c.children.forEach(function (b) {
                      if (b.level == 2) {
                        b.expand = false;
                      }
                    });
                  }
                });
              }
            } else if (expandLevel == 2) {
              if (e.level == 0) {
                e.expand = true;
              }
              if (e.children && e.children.length > 0) {
                e.children.forEach(function (c) {
                  if (c.level == 1) {
                    c.expand = false;
                  }
                  if (c.children && c.children.length > 0) {
                    c.children.forEach(function (b) {
                      if (b.level == 2) {
                        b.expand = false;
                      }
                    });
                  }
                });
              }
            } else if (expandLevel == 3) {
              if (e.level == 0) {
                e.expand = true;
              }
              if (e.children && e.children.length > 0) {
                e.children.forEach(function (c) {
                  if (c.level == 1) {
                    c.expand = true;
                  }
                  if (c.children && c.children.length > 0) {
                    c.children.forEach(function (b) {
                      if (b.level == 2) {
                        b.expand = false;
                      }
                    });
                  }
                });
              }
            }
          });
          this.data = res.result;
          let str = JSON.stringify(res.result);
          this.dataEdit = JSON.parse(str);
          // 头部加入一级
          let first = {
            id: "0",
            level: -1,
            title: "一级菜单",
          };
          this.dataEdit.unshift(first);
        }
      });
    },
    search() {
      if (this.searchKey) {
        this.loading = true;
        searchPermission({ title: this.searchKey }).then((res) => {
          this.loading = false;
          if (res.success) {
            this.data = res.result;
            this.tableData = res.result;
          }
        });
      } else {
        this.getAllList();
        this.getParentList();
      }
    },
    selectTree(v) {
      if (v.length > 0) {
        this.$refs.form.resetFields();
        // 转换null为""
        for (let attr in v[0]) {
          if (v[0][attr] == null) {
            v[0][attr] = "";
          }
        }
        let str = JSON.stringify(v[0]);
        let data = JSON.parse(str);
        this.form = data;
        this.editTitle = data.title;
      } else {
        this.cancelEdit();
      }
    },
    selectTreeEdit(v) {
      if (v.length > 0) {
        // 转换null为""
        for (let attr in v[0]) {
          if (v[0][attr] == null) {
            v[0][attr] = "";
          }
        }
        let str = JSON.stringify(v[0]);
        let data = JSON.parse(str);
        if (this.form.id == data.id) {
          this.$Message.warning("请勿选择自己作为父节点");
          v[0].selected = false;
          return;
        }
        // 验证级别level是否正确
        if (this.form.level - 1 != data.level) {
          this.$Message.warning("请选择一个" + this.form.level + "级菜单节点");
          v[0].selected = false;
          return;
        }
        this.form.parentId = data.id;
        let level = data.level + 1;
        if (level < 0) {
          level = 0;
        }
        this.form.level = level;
        this.form.parentTitle = data.title;
      }
    },
    cancelEdit() {
      let data = this.$refs.tree.getSelectedNodes()[0];
      if (data) {
        data.selected = false;
      }
      this.$refs.form.resetFields();
      this.form.id = "";
      this.editTitle = "";
    },
    handleReset() {
      let type = this.form.type;
      this.$refs.form.resetFields();
      this.form.icon = "";
      this.form.component = "";
      this.form.type = type;
    },
    submitEdit() {
      this.$refs.form.validate((valid) => {
        if (valid) {
          if (!this.form.id) {
            this.$Message.warning("请先点击选择要修改的菜单节点");
            return;
          }
          this.submitLoading = true;
          if (this.form.sortOrder == null) {
            this.form.sortOrder = "";
          }
          if (this.form.buttonType == null) {
            this.form.buttonType = "";
          }
          if (this.form.type == 1) {
            this.form.name = "";
            this.form.icon = "";
            this.form.component = "";
          }
          editPermission(this.form).then((res) => {
            this.submitLoading = false;
            if (res.success) {
              this.editTitle = this.form.title;
              this.$Message.success("编辑成功");
              // 标记重新获取菜单数据
              this.$store.commit("setAdded", false);
              util.initRouter(this);
              this.init();
            }
          });
        }
      });
    },
    changeEditUrl(e) {
      let v = e.target.value;
      if (v.indexOf("http") > -1) {
        this.form.component = "sys/monitor/monitor";
      }
    },
    addRoot() {
      this.isRoot = true;
      this.showAdd = true;
    },
    add() {
      if (!this.form.id) {
        this.$Message.warning("请先点击选择一个菜单权限节点");
        return;
      }
      if (this.form.level == 3) {
        this.$Modal.warning({
          title: "抱歉，无法继续添加",
          content: "左侧菜单最多仅支持二级菜单目录",
        });
        return;
      }
      this.isRoot = false;
      this.showAdd = true;
    },
    tableAdd(v) {
      this.form = v;
      this.add();
      this.editTitle = "";
      let data = this.$refs.tree.getSelectedNodes()[0];
      if (data) {
        data.selected = false;
      }
    },
    addSuccess() {
      // 标记重新获取菜单数据
      this.$store.commit("setAdded", false);
      util.initRouter(this);
      this.init();
    },
    changeSelect(v) {
      this.selectList = v;
    },
    showSelect(e) {
      this.selectList = e;
    },
    clearSelectAll() {
      this.$refs.table.selectAll(false);
    },
    remove(v) {
      this.selectList = [];
      this.selectList.push(v);
      this.delAll();
    },
    delAll() {
      if (this.selectList.length <= 0) {
        this.$Message.warning("您还未勾选要删除的数据");
        return;
      }
      this.$Modal.confirm({
        title: "确认删除",
        content:
          "您确认要删除所选的 " +
          this.selectList.length +
          " 条数据及其下级所有数据?",
        loading: true,
        onOk: () => {
          let ids = "";
          this.selectList.forEach(function (e) {
            ids += e.id + ",";
          });
          ids = ids.substring(0, ids.length - 1);
          deletePermission({ ids: ids }).then((res) => {
            this.$Modal.remove();
            if (res.success) {
              this.$Message.success("删除成功");
              // 标记重新获取菜单数据
              this.$store.commit("setAdded", false);
              util.initRouter(this);
              this.selectList = [];
              this.cancelEdit();
              this.init();
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