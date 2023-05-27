<template>
  <div class="menu-add">
    <!-- Drawer抽屉 -->
    <Drawer
      :title="isRoot ? '添加顶部菜单' : '添加子节点'"
      v-model="visible"
      width="500"
      draggable
      :mask="showMask"
      :mask-closable="false"
    >
      <div :style="{ maxHeight: maxHeight }" class="drawer-content">
        <Form label-colon>
          <FormItem label="上级节点" v-show="!isRoot">{{
            parentTitle
          }}</FormItem>
          <FormItem label="类型" prop="type">
            <div v-show="formAdd.type == -1">
              <Icon
                type="ios-navigate-outline"
                size="16"
                style="margin-right: 5px"
              ></Icon>
              <span>顶部菜单</span>
            </div>
            <div v-show="formAdd.type == 0">
              <Icon
                type="ios-list-box-outline"
                size="16"
                style="margin-right: 5px"
              ></Icon>
              <span>页面菜单</span>
            </div>
            <div v-show="formAdd.type == 1">
              <Icon
                type="md-radio-button-on"
                size="16"
                style="margin-right: 5px"
              ></Icon>
              <span>操作按钮</span>
            </div>
          </FormItem>
        </Form>
        <Form
          ref="formAdd"
          :model="formAdd"
          label-position="top"
        >
          <FormItem
            label="名称"
            prop="title"
            v-if="formAdd.type == -1 || formAdd.type == 0"
          >
            <div style="display: flex">
              <div style="width: 100%">
                <Input v-model="formAdd.title">
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
                          <i-switch v-model="formAdd.localize" size="large">
                            <span slot="open">开</span>
                            <span slot="close">关</span>
                          </i-switch>
                        </FormItem>
                        <FormItem label="多语言Key">
                          <Input
                            v-model="formAdd.i18n"
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
            v-if="formAdd.type == 1"
            class="block-tool"
          >
            <Tooltip placement="right" content="操作按钮名称不得重复">
              <Input v-model="formAdd.title" />
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
            v-if="formAdd.type == 0"
          >
            <Input v-model="formAdd.path" />
          </FormItem>
          <FormItem
            label="请求路径"
            prop="path"
            :required="false"
            v-if="formAdd.type == 1"
            class="block-tool"
          >
            <Tooltip
              placement="right"
              :max-width="230"
              transfer
              content="填写后端请求URL，后端将作权限拦截，若无可填写'无'或其他"
            >
              <Input v-model="formAdd.path" />
            </Tooltip>
          </FormItem>
          <FormItem
            label="按钮权限类型"
            prop="buttonType"
            v-if="formAdd.type == 1"
          >
            <dict
              dict="permission_type"
              v-model="formAdd.buttonType"
              placeholder="请选择或输入搜索"
              filterable
              clearable
              transfer
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
            v-if="formAdd.type == -1"
            class="block-tool"
          >
            <Tooltip placement="right" content="需唯一">
              <Input v-model="formAdd.name" />
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
            v-if="formAdd.type == 0"
            class="block-tool"
          >
            <Tooltip placement="right" content="需唯一">
              <Input v-model="formAdd.name" />
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
            v-if="formAdd.type == -1 || formAdd.type == 0"
          >
            <icon-choose
              showCustom
              showInput
              v-model="formAdd.icon"
            ></icon-choose>
          </FormItem>
          <FormItem label="显示红点" prop="component" v-if="formAdd.type == -1">
            <i-switch
              size="large"
              v-model="formAdd.component"
              :true-value="'hot'"
              :false-value="'normal'"
            >
              <span slot="open">开</span>
              <span slot="close">关</span>
            </i-switch>
          </FormItem>
          <FormItem
            label="系统站内菜单"
            prop="isMenu"
            v-if="formAdd.type == -1"
          >
            <i-switch size="large" v-model="formAdd.isMenu">
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
            v-if="formAdd.type == -1 && !formAdd.isMenu"
          >
            <Input v-model="formAdd.url" placeholder="Http链接或路由名" />
          </FormItem>
          <FormItem
            label="打开方式"
            prop="description"
            :rules="{
              required: true,
              message: '请选择打开方式',
              trigger: 'change',
            }"
            v-if="formAdd.type == -1 && !formAdd.isMenu"
          >
            <Select v-model="formAdd.description" transfer placeholder="请选择">
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
            v-if="formAdd.type == 0"
            :rules="{
              required: true,
              message: '请输入前端组件',
              trigger: 'blur',
            }"
          >
            <Input v-model="formAdd.component" />
          </FormItem>
          <FormItem
            label="第三方链接"
            prop="url"
            v-if="formAdd.type == 0 && formAdd.level == 2"
            :required="false"
          >
            <Input
              v-model="formAdd.url"
              placeholder="http://"
              @on-change="changeAddUrl"
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
                v-model="formAdd.sortOrder"
              ></InputNumber>
            </Tooltip>
          </FormItem>
          <FormItem
            label="始终显示"
            prop="showAlways"
            v-if="formAdd.level == 1"
            class="block-tool"
          >
            <i-switch size="large" v-model="formAdd.showAlways">
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
              v-model="formAdd.status"
              :true-value="0"
              :false-value="-1"
            >
              <span slot="open">启用</span>
              <span slot="close">禁用</span>
            </i-switch>
          </FormItem>
        </Form>
      </div>
      <div class="drawer-footer br">
        <div class="footer-between">
          <div>
            <Button type="primary" :loading="submitLoading" @click="submit"
              >提交</Button
            >
            <Button @click="visible = false">取消</Button>
          </div>
          <div class="flex-center">
            <!-- 遮罩： -->
            <i-switch size="large" v-model="showMask">
              <span slot="open">遮罩</span>
              <span slot="close">关闭</span>
            </i-switch>
          </div>
        </div>
      </div>
    </Drawer>
  </div>
</template>

<script>
import { addPermission } from "@/api/index";
import dict from "@/views/my-components/bigdata/dict";
import IconChoose from "@/views/my-components/bigdata/icon-choose";
export default {
  name: "add",
  components: {
    dict,
    IconChoose,
  },
  props: {
    value: {
      type: Boolean,
      default: false,
    },
    isRoot: {
      type: Boolean,
      default: false,
    },
    form: {
      type: Object,
    },
    dataLength: {
      type: Number,
    },
  },
  data() {
    return {
      showMask: true,
      visible: this.value,
      parentTitle: "",
      submitLoading: false,
      maxHeight: 510,
      formAdd: {},
    };
  },
  methods: {
    init() {},
    changeAddUrl(e) {
      let v = e.target.value;
      if (v.indexOf("http") > -1) {
        this.formAdd.component = "sys/monitor/monitor";
      }
    },
    submit() {
      this.$refs.formAdd.validate((valid) => {
        if (valid) {
          // 添加
          this.submitLoading = true;
          if (this.formAdd.type == 1) {
            this.formAdd.name = "";
            this.formAdd.icon = "";
            this.formAdd.component = "";
          }
          addPermission(this.formAdd).then((res) => {
            this.submitLoading = false;
            if (res.success) {
              this.$Message.success("操作成功");
              this.$emit("on-submit", true);
              this.visible = false;
            }
          });
        }
      });
    },
    setCurrentValue(value) {
      if (value === this.visible) {
        return;
      }
      this.$refs.formAdd.resetFields();
      if (this.isRoot) {
        // 添加顶部菜单
        this.formAdd = {
          type: -1,
          level: 0,
          parentId: 0,
          isMenu: true,
          sortOrder: this.dataLength + 1,
          status: 0,
          component: "normal",
        };
      } else {
        this.parentTitle = this.form.title;
        let type = 0;
        if (this.form.level == 2) {
          type = 1;
        }
        if (!this.form.children) {
          this.form.children = [];
        }
        this.formAdd = {
          icon: "",
          type: type,
          parentId: this.form.id,
          level: Number(this.form.level) + 1,
          sortOrder: this.form.children.length + 1,
          buttonType: "",
          status: 0,
          showAlways: true,
        };
        if (this.form.level == 0) {
          this.formAdd.path = "/";
          this.formAdd.component = "Main";
        }
      }
      // 显示
      this.visible = value;
    },
  },
  watch: {
    value(val) {
      this.setCurrentValue(val);
    },
    visible(value) {
      this.$emit("input", value);
    },
  },
  mounted() {
    this.init();
    this.maxHeight = Number(document.documentElement.clientHeight - 121) + "px";
  },
};
</script>

<style lang="less">
@import "@/styles/drawer-common.less";
@import "./menuManage.less";
.footer-between {
  display: flex;
  justify-content: space-between;
  align-items: center;
  .flex-center {
    display: flex;
    align-items: center;
  }
}
</style>

