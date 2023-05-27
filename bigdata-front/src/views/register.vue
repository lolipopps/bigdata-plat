<template>
  <div class="register">
    <Row
      type="flex"
      justify="center"
      align="middle"
      @keydown.enter.native="submitRegist"
      style="height: 100%"
    >
      <Col class="content">
        <div>
          <Header />
          <Form ref="registForm" :model="form" :rules="rules" class="form">
            <span class="register-title">{{ $t("register") }}</span>
            <FormItem prop="username">
              <Input
                v-model="form.username"
                :maxlength="16"
                size="large"
                clearable
                placeholder="请输入注册登录账号"
              />
            </FormItem>
            <FormItem prop="email">
              <Input
                v-model="form.email"
                size="large"
                clearable
                placeholder="请输入邮箱"
              />
            </FormItem>
            <FormItem prop="password">
              <SetPassword
                size="large"
                v-model="form.password"
                @on-change="changeInputPass"
              />
            </FormItem>
            <FormItem prop="mobile">
              <Input
                v-model="form.mobile"
                size="large"
                clearable
                placeholder="请输入手机号"
              >
                <Select v-model="select" slot="prepend" style="width: 70px">
                  <Option value="86">+86</Option>
                </Select>
              </Input>
            </FormItem>
            <FormItem prop="code" :error="errorCode">
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
          </Form>
          <Row type="flex" justify="space-between">
            <Button
              class="register-btn"
              type="primary"
              size="large"
              :loading="loading"
              @click="submitRegist"
            >
              <span v-if="!loading">{{ $t("register") }}</span>
              <span v-else>{{ $t("registering") }}</span>
            </Button>
            <router-link to="/login">
              <a class="to-login">{{ $t("loginNow") }}</a>
            </router-link>
          </Row>
        </div>
        <Footer />
      </Col>
      <LangSwitch />
    </Row>

    <ImgCaptcha
      v-model="captchaModal"
      @on-captchaId="captchaId = $event"
      @on-code="code = $event"
      @on-sumbit="sendSmsCode"
    />
  </div>
</template>

<script>
import { register, sendRegistSms } from "@/api/index";
import {
  validateUsername,
  validateMobile,
  validatePassword,
} from "@/libs/validate";
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
    return {
      captchaModal: false,
      getSms: "获取验证码",
      error: false,
      loading: false,
      sending: false,
      select: "86",
      errorCode: "",
      captchaId: "",
      code: "",
      form: {
        username: "",
        password: "",
        mobile: "",
        code: "",
      },
      rules: {
        username: [
          {
            required: true,
            message: "请输入注册登录账号",
            trigger: "blur",
          },
          { validator: validateUsername, trigger: "change" },
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
        email: [
          { required: true, message: "请输入邮箱地址" },
          { type: "email", message: "邮箱格式不正确" },
        ],
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
      },
    };
  },
  methods: {
    showCaptcha() {
      this.$refs.registForm.validate((valid) => {
        if (valid) {
          this.captchaModal = true;
        }
      });
    },
    sendSmsCode() {
      this.sending = true;
      this.getSms = "发送中";
      let params = {
        captchaId: this.captchaId,
        code: this.code,
      };
      sendRegistSms(this.form.mobile, params).then((res) => {
        this.getSms = "获取验证码";
        this.sending = false;
        if (res.success) {
          this.$Message.success("发送短信验证码成功");
          // 开始倒计时
          this.$refs.countDown.startCountDown();
        }
      });
    },
    changeInputPass(v, grade, strength) {
      this.form.passStrength = strength;
    },
    submitRegist() {
      this.$refs.registForm.validate((valid) => {
        if (valid) {
          if (!this.form.code) {
            this.errorCode = "请输入验证码";
            return;
          } else {
            this.errorCode = "";
          }
          this.loading = true;
          register(this.form).then((res) => {
            this.loading = false;
            if (res.success) {
              let query = {
                username: this.form.username,
              };
              this.$router.push({
                name: "register-result",
                query: query,
              });
            }
          });
        }
      });
    },
  },
  mounted() {},
};
</script>

<style lang="less">
@import "./register.less";
</style>
