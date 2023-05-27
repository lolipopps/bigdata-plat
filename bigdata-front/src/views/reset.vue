<template>
  <div class="reset-wrap">
    <Row
      type="flex"
      justify="center"
      align="middle"
      @keydown.enter.native="submitReset"
      style="height: 100%"
    >
      <Col class="content">
        <div>
          <Header />
          <div class="reset">
            <Tabs value="1">
              <TabPane :label="label" name="1" :icon="icon"></TabPane>
            </Tabs>
            <Form ref="form" :model="form" :rules="rules" class="form">
              <div v-if="type == '0'">
                <FormItem prop="mobile" :error="errorMobile">
                  <Input
                    v-model="form.mobile"
                    size="large"
                    clearable
                    placeholder="请输入注册绑定手机号"
                  ></Input>
                </FormItem>
                <FormItem prop="code">
                  <Row type="flex" justify="space-between">
                    <Input
                      v-model="form.code"
                      size="large"
                      clearable
                      placeholder="请输入短信验证码"
                      :maxlength="10"
                      class="input-verify"
                    />
                    <CountDownButton
                      ref="countDown"
                      @on-click="showCaptcha"
                      :autoCountDown="false"
                      size="large"
                      :loading="sending"
                      :text="getSms"
                    />
                  </Row>
                </FormItem>
              </div>
              <div v-else>
                <FormItem prop="email" :error="errorEmail">
                  <Input
                    v-model="form.email"
                    size="large"
                    clearable
                    placeholder="请输入绑定邮箱"
                  ></Input>
                </FormItem>
                <FormItem prop="code">
                  <Row type="flex" justify="space-between">
                    <Input
                      v-model="form.code"
                      size="large"
                      clearable
                      placeholder="请输入您收到的邮件中的验证码"
                      :maxlength="10"
                      class="input-verify"
                    />
                    <CountDownButton
                      ref="countDownEmail"
                      @on-click="showCaptcha"
                      :autoCountDown="false"
                      size="large"
                      :loading="sending"
                      :text="getSms"
                    />
                  </Row>
                </FormItem>
              </div>
              <FormItem prop="password">
                <SetPassword
                  v-model="form.password"
                  size="large"
                  @on-change="changeInputPass"
                  placeholder="请输入新的密码，长度为6-20个字符"
                />
              </FormItem>
              <FormItem prop="confirmPass">
                <Input
                  type="password"
                  :maxlength="20"
                  v-model="form.confirmPass"
                  size="large"
                  clearable
                  placeholder="请再次输入确认新的密码"
                />
              </FormItem>
            </Form>
            <Button
              style="margin-bottom: 3vh"
              type="primary"
              size="large"
              :loading="loading"
              @click="submitReset"
              long
              >重置密码</Button
            >
            <Row type="flex">
              <a @click="$router.go(-1)" class="back">
                <Icon
                  type="md-arrow-round-back"
                  style="margin-right: 3px"
                />返回
              </a>
            </Row>
          </div>
        </div>
        <Footer />
      </Col>
      <LangSwitch />
    </Row>

    <ImgCaptcha
      v-model="captchaModal"
      @on-captchaId="form.captchaId = $event"
      @on-code="form.code = $event"
      @on-sumbit="codeSubmit"
    />
  </div>
</template>

<script>
import {
  sendResetSms,
  resetByMobile,
  sendResetEmail,
  resetByEmail,
} from "@/api/index";
import { validateMobile, validatePassword } from "@/libs/validate";
import Header from "@/views/main-components/header";
import Footer from "@/views/main-components/footer";
import LangSwitch from "@/views/main-components/lang-switch";
import CountDownButton from "@/views/my-components/bigdata/count-down-button";
import SetPassword from "@/views/my-components/bigdata/set-password";
import ImgCaptcha from "@/views/my-components/bigdata/img-captcha";
export default {
  components: {
    CountDownButton,
    LangSwitch,
    SetPassword,
    Header,
    Footer,
    ImgCaptcha,
  },
  data() {
    const validateConfirmPass = (rule, value, callback) => {
      if (value.length < 6) {
        callback(new Error("密码长度不得小于6位"));
      } else if (value !== this.form.password) {
        callback(new Error("两次输入的密码不一致"));
      } else {
        callback();
      }
    };
    return {
      captchaModal: false,
      label: "使用手机号重置密码",
      icon: "md-phone-portrait",
      loading: false,
      type: "0",
      form: {
        mobile: "",
        email: "",
        password: "",
        passStrength: "",
        captchaId: "",
        code: "",
      },
      sending: false,
      getSms: "获取验证码",
      errorMobile: "",
      errorEmail: "",
      rules: {
        mobile: [
          {
            required: true,
            message: "请输入手机号",
            trigger: "blur",
          },
          {
            validator: validateMobile,
            trigger: "blur",
          },
        ],
        email: [
          {
            required: true,
            message: "请输入邮箱地址",
            trigger: "blur",
          },
          {
            type: "email",
            message: "邮箱格式不正确",
            trigger: "blur",
          },
        ],
        password: [
          {
            required: true,
            message: "请输入密码",
            trigger: "blur",
          },
          {
            validator: validatePassword,
            trigger: "blur",
          },
        ],
        code: [
          {
            required: true,
            message: "请输入验证码",
            trigger: "blur",
          },
        ],
        confirmPass: [
          {
            required: true,
            message: "请输入密码",
            trigger: "blur",
          },
          {
            validator: validateConfirmPass,
            trigger: "blur",
          },
        ],
      },
    };
  },
  methods: {
    codeSubmit() {
      if (this.type == "0") {
        this.sendSmsCode();
      } else if (this.type == "1") {
        this.sendEmailCode();
      }
    },
    showCaptcha() {
      if (this.type == "0") {
        var reg = /^[1][3,4,5,6,7,8][0-9]{9}$/;
        if (!reg.test(this.form.mobile)) {
          this.errorMobile = "手机号格式错误";
          return;
        } else {
          this.errorMobile = "";
        }
      } else if (this.type == "1") {
        var reg =
          /^[A-Za-z\d]+([-_.][A-Za-z\d]+)*@([A-Za-z\d]+[-.])+[A-Za-z\d]{2,4}$/;
        if (!reg.test(this.form.email)) {
          this.errorEmail = "邮箱格式错误";
          return;
        } else {
          this.errorEmail = "";
        }
      }
      this.captchaModal = true;
    },
    sendSmsCode() {
      this.sending = true;
      this.getSms = "发送中";
      sendResetSms(this.form.mobile, this.form).then((res) => {
        this.getSms = "获取验证码";
        this.sending = false;
        if (res.success) {
          this.$Message.success("发送短信验证码成功");
          // 开始倒计时
          this.$refs.countDown.startCountDown();
        }
      });
    },
    sendEmailCode() {
      this.sending = true;
      this.getSms = "发送中";
      sendResetEmail(this.form.email, this.form).then((res) => {
        this.getSms = "获取验证码";
        this.sending = false;
        if (res.success) {
          this.$Message.success("发送邮件验证码成功，请注意查收");
          // 开始倒计时
          this.$refs.countDownEmail.startCountDown();
        }
      });
    },
    changeInputPass(v, grade, strength) {
      this.form.passStrength = strength;
    },
    submitReset() {
      this.$refs.form.validate((valid) => {
        if (valid) {
          this.loading = true;
          if (this.type == "0") {
            resetByMobile(this.form).then((res) => {
              this.loading = false;
              if (res.success) {
                this.$Message.success("重置密码成功，请牢记您的新密码");
                this.$router.push({
                  name: "login",
                });
              }
            });
          } else if (this.type == "1") {
            resetByEmail(this.form).then((res) => {
              this.loading = false;
              if (res.success) {
                this.$Message.success("重置密码成功，请牢记您的新密码");
                this.$router.push({
                  name: "login",
                });
              }
            });
          }
        }
      });
    },
  },
  mounted() {
    let type = this.$route.query.type;
    if (type == "1") {
      this.type = type;
      this.label = "使用邮箱重置密码";
      this.icon = "ios-mail-outline";
    }
  },
};
</script>

<style lang="less">
@import "./reset.less";
</style>
