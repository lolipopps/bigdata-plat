<style lang="less">
</style>
<template>
  <div style="display: flex; position: relative">
    <Form
      ref="smsForm"
      :model="sms"
      :label-width="110"
      label-position="left"
      :rules="smsValidate"
    >
      <FormItem label="服务提供商" prop="serviceName">
        <Select
          v-model="sms.serviceName"
          @on-change="changeSms"
          placeholder="请选择"
          style="width: 230px"
        >
          <Option value="ALI_SMS">阿里云</Option>
          <Option value="TENCENT_SMS">腾讯云</Option>
        </Select>
      </FormItem>
      <FormItem label="accessKey" prop="accessKey">
        <Input
          type="text"
          v-model="sms.accessKey"
          placeholder="请输入accessKey/secretId"
          :disabled="changeLoading"
          style="width: 380px"
        />
      </FormItem>
      <FormItem label="secretKey" prop="secretKey">
        <Tooltip placement="right" content="如需编辑请点击查看图标">
          <Input
            class="input-see"
            type="text"
            v-model="sms.secretKey"
            :readonly="!changedSmsSK"
            @on-click="seeSecret(sms.serviceName)"
            :disabled="changeLoading"
            icon="ios-eye"
            placeholder="请输入secretKey"
            style="width: 380px"
          />
        </Tooltip>
      </FormItem>
      <FormItem
        v-if="sms.serviceName == 'TENCENT_SMS'"
        label="应用ID"
        prop="appId"
      >
        <Input
          type="text"
          v-model="sms.appId"
          :disabled="changeLoading"
          placeholder="请输入应用ID（短信控制台-应用管理）"
          style="width: 380px"
        />
      </FormItem>
      <FormItem label="短信签名" prop="signName">
        <Input
          type="text"
          v-model="sms.signName"
          :disabled="changeLoading"
          placeholder="请输入短信签名，例如bigdata"
          style="width: 380px"
        />
      </FormItem>
      <FormItem label="短信模版管理" class="form-noheight">
        <Table
          border
          size="small"
          :columns="columns"
          :data="data"
          style="width: 700px"
        >
          <template slot-scope="{ row, index }" slot="templateCode">
            <Input
              :disabled="saving"
              v-model="editTemplateCode"
              v-if="editIndex === index"
              placeholder="请输入短信模版CODE/ID"
            />
            <span v-else>{{ row.templateCode }}</span>
          </template>

          <template slot-scope="{ row, index }" slot="action">
            <div v-if="editIndex === index">
              <a @click="handleSave(index)">保存</a>
              <Divider type="vertical" />
              <a @click="editIndex = -1">取消</a>
            </div>
            <div v-else>
              <a @click="handleEdit(row, index)">编辑</a>
            </div>
          </template>
        </Table>
      </FormItem>
      <FormItem>
        <Button
          type="primary"
          style="width: 100px"
          :disabled="changeLoading"
          :loading="saveLoading"
          @click="saveEditSms"
          >保存更改</Button
        >
      </FormItem>
    </Form>
    <Spin fix v-if="loading"></Spin>
  </div>
</template>

<script>
import {
  checkSmsSet,
  seeSecretSet,
  getSmsSet,
  editSmsSet,
  getSmsTemplateCode,
  setSmsTemplateCode,
} from "@/api/index";
export default {
  name: "sms",
  data() {
    return {
      loading: false, // 表单加载状态
      changeLoading: false,
      saveLoading: false,
      sms: {
        serviceName: "ALI_SMS",
        accessKey: "",
        secretKey: "",
        appId: "",
        signName: "",
        type: "",
        templateCode: "",
      },
      changedSmsSK: false, // 是否修改短信的secrectKey
      smsValidate: {
        // 表单验证规则
        serviceName: [{ required: true, message: "请选择", trigger: "change" }],
        accessKey: [{ required: true, message: "请输入", trigger: "blur" }],
        secretKey: [{ required: true, message: "请输入", trigger: "blur" }],
        appId: [{ required: true, message: "请输入", trigger: "blur" }],
        signName: [{ required: true, message: "请输入", trigger: "blur" }],
      },
      columns: [
        {
          title: "使用场景（类型）",
          key: "type",
          width: "300",
          render: (h, params) => {
            let v = params.row.type,
              str = "";
            if (v == "SMS_COMMON") {
              str = "通用验证码";
            } else if (v == "SMS_REGISTER") {
              str = "注册";
            } else if (v == "SMS_LOGIN") {
              str = "登录";
            } else if (v == "SMS_CHANGE_MOBILE") {
              str = "修改绑定手机号";
            } else if (v == "SMS_CHANGE_PASS") {
              str = "修改密码";
            } else if (v == "SMS_RESET_PASS") {
              str = "重置密码";
            } else if (v == "SMS_ACTIVITI") {
              str = "工作流消息";
            }
            str += "（" + v + "）";
            return h("span", str);
          },
        },
        {
          title: "短信模版CODE/ID",
          slot: "templateCode",
        },
        {
          title: "操作",
          slot: "action",
          width: "120",
        },
      ],
      data: [],
      editIndex: -1,
      editType: "",
      editTemplateCode: "",
      saving: false,
    };
  },
  methods: {
    init() {
      this.initSmsSet();
    },
    initSmsSet() {
      this.loading = true;
      checkSmsSet().then((res) => {
        if (res.success && res.result) {
          let sms = res.result;
          getSmsSet(sms).then((res) => {
            this.loading = false;
            if (res.result) {
              // 转换null为""
              for (let attr in res.result) {
                if (res.result[attr] == null) {
                  res.result[attr] = "";
                }
              }
              this.sms = res.result;
              this.sms.type = "0";
              this.changeSmsType(this.sms.type);
            } else {
              this.changedSmsSK = true;
            }
          });
        } else {
          this.loading = false;
          this.changedSmsSK = true;
        }
      });
    },
    changeSms(v) {
      this.$refs.smsForm.resetFields();
      this.changeLoading = true;
      getSmsSet(v).then((res) => {
        this.changeLoading = false;
        if (res.result) {
          // 转换null为""
          for (let attr in res.result) {
            if (res.result[attr] == null) {
              res.result[attr] = "";
            }
          }
          this.sms = res.result;
          this.changedSmsSK = false;
          this.sms.type = "0";
          this.changeSmsType(this.sms.type);
        } else {
          this.sms = { serviceName: v };
          this.sms.type = "0";
          this.changedSmsSK = true;
        }
      });
    },
    seeSecret(name) {
      if (!name) {
        return;
      }
      seeSecretSet(name).then((res) => {
        if (res.success) {
          this.sms.secretKey = res.result;
          this.changedSmsSK = true;
        }
      });
    },
    changeSmsType(v) {
      if (v == "" || v == null || v == "undefined") {
        return;
      }
      getSmsTemplateCode(this.sms.serviceName).then((res) => {
        if (res.success) {
          this.data = res.result;
        }
      });
    },
    handleEdit(row, index) {
      this.editType = row.type;
      this.editTemplateCode = row.templateCode;
      this.editIndex = index;
    },
    handleSave(index) {
      this.data[index].type = this.editType;
      this.data[index].templateCode = this.editTemplateCode;
      let params = this.data[index];
      params.serviceName = this.sms.serviceName;
      this.saving = true;
      setSmsTemplateCode(params).then((res) => {
        this.saving = false;
        if (res.success) {
          this.editIndex = -1;
          this.$Message.success("操作成功");
        }
      });
    },
    saveEditSms() {
      this.$refs.smsForm.validate((valid) => {
        if (valid) {
          this.saveLoading = true;
          this.sms.changed = this.changedSmsSK;
          editSmsSet(this.sms).then((res) => {
            this.saveLoading = false;
            if (res.success) {
              this.$Message.success("保存成功");
            }
          });
        }
      });
    },
  },
  mounted() {
    this.init();
  },
};
</script>