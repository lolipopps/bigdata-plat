<template>
  <div>
    <div class="item">
      <div>
        <div class="title">账户密码</div>
        <div v-if="form.passStrength" class="desc">
          当前密码强度：
          <span v-if="form.passStrength == '弱'" class="red">{{
            form.passStrength
          }}</span>
          <span v-if="form.passStrength == '中'" class="middle">{{
            form.passStrength
          }}</span>
          <span v-if="form.passStrength == '强'" class="green">{{
            form.passStrength
          }}</span>
        </div>
      </div>
      <div>
        <a @click="showChangePass = true">修改</a>
      </div>
    </div>
    <div class="item">
      <div>
        <div class="title">绑定手机</div>
        <div class="desc">
          <span v-if="form.mobile">已绑定手机：{{ form.mobile }}</span>
          <span v-else>未绑定手机号</span>
        </div>
      </div>
      <div>
        <a @click="showChangeMobile">修改</a>
      </div>
    </div>
    <div class="item">
      <div>
        <div class="title">绑定邮箱</div>
        <div class="desc">
          <span v-if="form.email">已绑定邮箱：{{ form.email }}</span>
          <span v-else>未绑定邮箱</span>
        </div>
      </div>
      <div>
        <a @click="showChangeEmail">修改</a>
      </div>
    </div>
    <div class="item">
      <div>
        <div class="title">密保问题</div>
        <div class="desc">未设置密保问题，密保问题可有效保护账户安全</div>
      </div>
      <div>
        <a>暂不支持设置</a>
      </div>
    </div>

    <Modal
      title="修改手机号"
      v-model="editMobileVisible"
      :closable="false"
      :mask-closable="false"
      :width="500"
    >
      <Form
        ref="mobileEditForm"
        :model="mobileEditForm"
        :label-width="70"
        :rules="mobileEditValidate"
      >
        <FormItem label="手机号" prop="mobile">
          <Input
            v-model="mobileEditForm.mobile"
            @on-change="hasChangePhone"
            placeholder="请输入新手机号"
          />
        </FormItem>
        <FormItem label="验证码" prop="code" :error="codeError">
          <Row type="flex" justify="space-between">
            <Input
              v-model="mobileEditForm.code"
              placeholder="请输入您收到的短信验证码"
              style="width: 280px"
            />
            <CountDownButton
              ref="countDownMobile"
              @on-click="sendEditMobileCode"
              :disabled="canSendMobileCode"
              :autoCountDown="false"
              :loading="sending"
              :text="getSms"
            />
          </Row>
        </FormItem>
      </Form>
      <div slot="footer">
        <Button type="text" @click="cancelInputCodeBox">取消</Button>
        <Button
          type="primary"
          :loading="checkCodeLoading"
          @click="submitEditMobile"
          >提交</Button
        >
      </div>
    </Modal>

    <Modal
      title="修改邮箱"
      v-model="editEmailVisible"
      :closable="false"
      :mask-closable="false"
      :width="500"
    >
      <Form
        ref="emailEditForm"
        :model="emailEditForm"
        :label-width="100"
        :rules="emailEditValidate"
      >
        <FormItem label="新邮箱地址" prop="email">
          <Input
            v-model="emailEditForm.email"
            @on-change="hasChangeEmail"
            placeholder="请输入新邮箱地址"
          ></Input>
        </FormItem>
        <FormItem label="验证码" prop="code" :error="codeError">
          <Row type="flex" justify="space-between">
            <Input
              v-model="emailEditForm.code"
              placeholder="请输入您收到的邮件中的验证码"
              style="width: 250px"
            />
            <CountDownButton
              ref="countDownEmail"
              @on-click="sendVerifyEmail"
              :disabled="canSendEditEmail"
              :autoCountDown="false"
              :loading="sending"
              :text="getSms"
            />
          </Row>
        </FormItem>
      </Form>
      <div slot="footer">
        <Button type="text" @click="cancelEditEmail">取消</Button>
        <Button
          type="primary"
          :loading="editEmailLoading"
          @click="submitEditEmail"
          >提交</Button
        >
      </div>
    </Modal>

    <!-- 修改密码 -->
    <changePass v-model="showChangePass" />
    <!-- 验证密码 -->
    <check-password ref="checkPassMobile" @on-success="checkSuccessMobile" />
    <!-- 验证密码 -->
    <check-password ref="checkPassEmail" @on-success="checkSuccessEmail" />
  </div>
</template>

<script>
import {
  sendEditEmail,
  editEmail,
  sendEditMobileSms,
  changeMobile,
} from "@/api/index";
import { validateMobile } from "@/libs/validate";
import CountDownButton from "@/views/my-components/bigdata/count-down-button";
import checkPassword from "@/views/my-components/bigdata/check-password";
import changePass from "@/views/change-pass/change-pass";
export default {
  components: {
    changePass,
    checkPassword,
    CountDownButton,
  },
  name: "security",
  data() {
    return {
      showChangePass: false,
      form: {},
      mobileEditForm: {
        mobile: "",
        code: "",
      },
      emailEditForm: {
        email: "",
        code: "",
      },
      codeError: "",
      initPhone: "",
      initEmail: "",
      saveLoading: false,
      sending: false,
      getSms: "获取验证码",
      canSendMobileCode: true, // 是否可点获取验证码
      checkCodeLoading: false,
      checkPassLoading: false,
      editEmailLoading: false,
      mobileEditValidate: {
        mobile: [
          { required: true, message: "请输入手机号码" },
          { validator: validateMobile },
        ],
      },
      emailEditValidate: {
        email: [
          { required: true, message: "请输入邮箱地址" },
          { type: "email", message: "邮箱格式不正确" },
        ],
      },
      editMobileVisible: false, // 显示填写验证码box
      editEmailVisible: false,
      canSendEditEmail: true,
    };
  },
  methods: {
    init() {
      let userInfo = this.getUserInfo();

      this.form = userInfo;
      this.initPhone = userInfo.mobile;
      this.mobileEditForm.mobile = userInfo.mobile;
      this.initEmail = userInfo.email;
      this.emailEditForm.email = userInfo.email;
    },
    showChangeMobile() {
      this.$refs.checkPassMobile.show();
    },
    checkSuccessMobile() {
      this.editMobileVisible = true;
    },
    showChangeEmail() {
      this.$refs.checkPassEmail.show();
    },
    checkSuccessEmail() {
      this.editEmailVisible = true;
    },
    cancelInputCodeBox() {
      this.editMobileVisible = false;
      this.form.mobile = this.initPhone;
    },
    cancelEditEmail() {
      this.editEmailVisible = false;
      this.emailEditForm.email = this.initEmail;
    },
    sendEditMobileCode() {
      this.$refs["mobileEditForm"].validate((valid) => {
        if (valid) {
          this.getSms = "发送中";
          this.sending = true;
          sendEditMobileSms(this.mobileEditForm.mobile).then((res) => {
            this.getSms = "获取验证码";
            this.sending = false;
            if (res.success) {
              this.$Message.success("发送短信验证码成功");
              // 开始倒计时
              this.$refs.countDownMobile.startCountDown();
            }
          });
        }
      });
    },
    submitEditMobile() {
      this.$refs["mobileEditForm"].validate((valid) => {
        if (valid) {
          if (!this.mobileEditForm.code) {
            this.codeError = "请填写短信验证码";
            return;
          } else {
            this.codeError = "";
          }
          this.checkCodeLoading = true;
          changeMobile(this.mobileEditForm).then((res) => {
            this.checkCodeLoading = false;
            if (res.success) {
              this.$Message.success("修改成功");
              this.form.mobile = this.mobileEditForm.mobile;
              this.initPhone = this.mobileEditForm.mobile;
              this.editMobileVisible = false;
              this.$emit("on-success", true);
            }
          });
        }
      });
    },
    hasChangePhone() {
      if (this.mobileEditForm.mobile == this.initPhone) {
        this.canSendMobileCode = true;
      } else {
        this.$refs["mobileEditForm"].validate((valid) => {
          if (valid) {
            this.canSendMobileCode = false;
          } else {
            this.canSendMobileCode = true;
          }
        });
      }
    },
    hasChangeEmail() {
      if (this.emailEditForm.email == this.initEmail) {
        this.canSendEditEmail = true;
      } else {
        this.canSendEditEmail = false;
      }
    },
    sendVerifyEmail() {
      this.$refs["emailEditForm"].validate((valid) => {
        if (valid) {
          this.getSms = "发送中";
          this.sending = true;
          sendEditEmail(this.emailEditForm.email).then((res) => {
            this.getSms = "获取验证码";
            this.sending = false;
            if (res.success) {
              this.$Message.success("发送邮件验证码成功，请注意查收");
              // 开始倒计时
              this.$refs.countDownEmail.startCountDown();
            }
          });
        }
      });
    },
    submitEditEmail() {
      this.$refs["emailEditForm"].validate((valid) => {
        if (valid) {
          if (!this.emailEditForm.code) {
            this.codeError = "请输入验证码";
            return;
          } else {
            this.codeError = "";
          }
          this.editEmailLoading = true;
          editEmail(this.emailEditForm).then((res) => {
            this.editEmailLoading = false;
            if (res.success) {
              this.initEmail = this.emailEditForm.email;
              this.form.email = this.emailEditForm.email;
              this.$Message.success("修改邮件成功");
              this.editEmailVisible = false;
              this.$emit("on-success", true);
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
