<template>
  <div class="login">
    <Row type="flex" justify="center" align="middle" @keydown.enter.native="submitLogin" style="height: 100%">
      <Col class="content">
      <div>
        <Header />
        <div v-if="!socialLogining">
          <div style="position: relative">
            <Tabs v-model="tabName" @on-click="changeTab">
              <TabPane :label="$t('usernameLogin')" name="username" icon="md-person">
                <Form ref="usernameLoginForm" :model="form" :rules="rules" class="form" v-if="tabName == 'username'">
                  <FormItem prop="username">
                    <Input v-model="form.username" prefix="ios-person-outline" size="large" clearable placeholder="账号/手机号/邮箱" autocomplete="off" />
                  </FormItem>
                  <FormItem prop="password">
                    <Input type="password" v-model="form.password" prefix="ios-lock-outline" size="large" password placeholder="请输入密码" autocomplete="off" />
                  </FormItem>
                  <FormItem prop="code" :rules="{ required: true, message: '请输入验证码' }">
                    <Row type="flex" justify="space-between" style="align-items: center; overflow: hidden">
                      <Input v-model="form.code" size="large" clearable placeholder="请输入图片验证码" :maxlength="10" class="input-verify" />
                      <div class="code-image" style="position: relative; font-size: 12px">
                        <Spin v-if="loadingCaptcha" fix></Spin>
                        <img :src="captchaImg" @click="getCaptchaImg" alt="加载验证码失败" style="
                              width: 110px;
                              cursor: pointer;
                              display: block;
                            " />
                      </div>
                    </Row>
                  </FormItem>
                </Form>
              </TabPane>
              <TabPane :label="$t('mobileLogin')" name="mobile" icon="ios-phone-portrait">
                <Form ref="mobileLoginForm" :model="form" :rules="rules" class="form" v-if="tabName == 'mobile'">
                  <FormItem prop="mobile">
                    <Input v-model="form.mobile" prefix="ios-phone-portrait" size="large" clearable placeholder="请输入手机号" />
                  </FormItem>
                  <FormItem prop="code" :rules="
                        checkSms
                          ? { required: true, message: '请输入验证码' }
                          : {}
                      ">
                    <Row type="flex" justify="space-between">
                      <Input v-model="form.code" prefix="ios-mail-outline" size="large" clearable placeholder="请输入短信验证码" :maxlength="6" class="input-verify" />
                      <CountDownButton ref="countDown" @on-click="sendSmsCode" :autoCountDown="false" size="large" :loading="sending" :text="getSms" />
                    </Row>
                  </FormItem>
                </Form>
              </TabPane>
            </Tabs>
            <!-- <Tooltip content="bigdata App扫码登录" placement="right" class="qr block-tool">
              <router-link to="/login-qr">
                <XIcon type="iconfont icon-saomadenglu1" size="30" />
              </router-link>
            </Tooltip> -->
          </div>

          <Row justify="space-between" align="middle">
            <Checkbox v-model="form.saveLogin" size="large">{{
                $t("autoLogin")
              }}</Checkbox>
            <Dropdown trigger="click" @on-click="handleDropDown">
              <a class="forget-pass">{{ $t("forgetPass") }}</a>
              <DropdownMenu slot="list">

                <DropdownItem name="resetByMobile">使用手机号重置密码</DropdownItem>
                <DropdownItem name="resetByEmail">使用邮箱重置密码</DropdownItem>
              </DropdownMenu>
            </Dropdown>
          </Row>
          <Button class="login-btn" type="primary" size="large" :loading="loading" @click="submitLogin" long>
            <span v-if="!loading">{{ $t("login") }}</span>
            <span v-else>{{ $t("logining") }}</span>
          </Button>

        </div>
        <div v-if="socialLogining">
          <RectLoading />
        </div>
      </div>
      <Footer />
      </Col>
      <LangSwitch />
    </Row>
  </div>
</template>

<script>
import {
  login,
  userInfo,
  githubLogin,
  qqLogin,
  weiboLogin,
  wechatLogin,
  dingdingLogin,
  workwechatLogin,
  getJWT,
  sendLoginSms,
  smsLogin,
  initCaptcha,
  drawCodeImage,
  getOtherSet,
  getNotice,
} from "@/api/index";
import { validateMobile } from "@/libs/validate";
import Cookies from "js-cookie";
import Header from "@/views/main-components/header";
import Footer from "@/views/main-components/footer";
import LangSwitch from "@/views/main-components/lang-switch";
import RectLoading from "@/views/my-components/bigdata/rect-loading";
import CountDownButton from "@/views/my-components/bigdata/count-down-button";
import util from "@/libs/util.js";
export default {
  components: {
    CountDownButton,
    RectLoading,
    LangSwitch,
    Header,
    Footer,
  },
  data() {
    return {
      showMore: false,
      captchaImg: "",
      loadingCaptcha: true,
      socialLogining: true,
      error: false,
      tabName: "username",
      getSms: "获取验证码",
      loading: false,
      sending: false,
      checkSms: true,
      form: {
        username: "",
        password: "123456",
        mobile: "",
        code: "",
        captchaId: "",
        saveLogin: true,
      },
      rules: {
        username: [
          {
            required: true,
            message: "请输入账号",
            trigger: "blur",
          },
        ],
        password: [
          {
            required: true,
            message: "请输入密码",
            trigger: "blur",
          },
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
    changeTab(v) {
      this.form.code = "";
    },
    getCaptchaImg() {
      this.loadingCaptcha = true;
      initCaptcha().then((res) => {
        this.loadingCaptcha = false;
        if (res.success) {
          this.form.code = "";
          this.form.captchaId = res.result;
          this.captchaImg = drawCodeImage + this.form.captchaId;
        }
      });
    },
    sendSmsCode() {
      this.checkSms = false;
      setTimeout(() => {
        this.$refs.mobileLoginForm.validate((valid) => {
          this.checkSms = true;
          if (valid) {
            this.sending = true;
            this.getSms = "发送中";
            sendLoginSms(this.form.mobile).then((res) => {
              this.getSms = "获取验证码";
              this.sending = false;
              if (res.success) {
                this.$Message.success("发送短信验证码成功");
                // 开始倒计时
                this.$refs.countDown.startCountDown();
              }
            });
          }
        });
      }, 50);
    },
    afterLogin(res) {
      let accessToken = res.result;
      this.setStore("accessToken", accessToken);
      getOtherSet().then((res) => {
        if (res.result) {
          let domain = res.result.ssoDomain;
          Cookies.set("accessToken", accessToken, {
            domain: domain,
            expires: 7,
          });
        }
      });
      // 获取用户信息
      userInfo().then((res) => {
        if (res.success) {
          // 避免超过大小限制
          delete res.result.permissions;
          let roles = [];
          res.result.roles.forEach((e) => {
            roles.push(e.name);
          });
          delete res.result.roles;
          this.setStore("roles", roles);
          this.setStore("saveLogin", this.form.saveLogin);
          if (this.form.saveLogin) {
            // 保存7天
            Cookies.set("userInfo", JSON.stringify(res.result), {
              expires: 7,
            });
          } else {
            Cookies.set("userInfo", JSON.stringify(res.result));
          }
          this.setStore("userInfo", res.result);
          this.$store.commit("setUserInfo", res.result);
          // 加载菜单
          util.initRouter(this);
          // window.location.reload();
          this.$router.push({
            name: "home_index",
          });
        } else {
          this.loading = false;
        }
      });
    },
    submitLogin() {
      if (this.tabName == "username") {
        this.$refs.usernameLoginForm.validate((valid) => {
          if (valid) {
            this.loading = true;
            login(this.form).then((res) => {
              if (res.success) {
                this.afterLogin(res);
              } else {
                this.loading = false;
                this.getCaptchaImg();
              }
            });
          }
        });
      } else if (this.tabName == "mobile") {
        this.$refs.mobileLoginForm.validate((valid) => {
          if (valid) {
            this.loading = true;
            smsLogin(this.form).then((res) => {
              if (res.success) {
                this.afterLogin(res);
              } else {
                this.loading = false;
              }
            });
          }
        });
      }
    },
    toGithubLogin() {
      this.socialLogining = true;
      githubLogin().then((res) => {
        if (res.success) {
          window.location.href = res.result;
        } else {
          this.socialLogining = false;
        }
      });
    },
    toQQLogin() {
      this.socialLogining = true;
      qqLogin().then((res) => {
        if (res.success) {
          window.location.href = res.result;
        } else {
          this.socialLogining = false;
        }
      });
    },
    toWeiboLogin() {
      this.socialLogining = true;
      weiboLogin().then((res) => {
        if (res.success) {
          window.location.href = res.result;
        } else {
          this.socialLogining = false;
        }
      });
    },
    toWeixinLogin() {
      this.socialLogining = true;
      wechatLogin().then((res) => {
        if (res.success) {
          window.location.href = res.result;
        } else {
          this.socialLogining = false;
        }
      });
    },
    toDingdingLogin() {
      this.socialLogining = true;
      dingdingLogin().then((res) => {
        if (res.success) {
          window.location.href = res.result;
        } else {
          this.socialLogining = false;
        }
      });
    },
    toWorkwechatLogin() {
      this.socialLogining = true;
      workwechatLogin().then((res) => {
        if (res.success) {
          window.location.href = res.result;
        } else {
          this.socialLogining = false;
        }
      });
    },
    relatedLogin() {
      let q = this.$route.query;
      let error = q.error;
      if (error !== "" && error !== undefined) {
        this.$Message.error(error);
      }
      let related = q.related;
      let JWTKey = q.JWTKey;
      if (related && related == "1" && JWTKey) {
        getJWT({ JWTKey: JWTKey }).then((res) => {
          if (res.success) {
            this.socialLogining = true;
            let accessToken = res.result;
            this.setStore("accessToken", accessToken);
            getOtherSet().then((res) => {
              if (res.result) {
                let domain = res.result.ssoDomain;
                Cookies.set("accessToken", accessToken, {
                  domain: domain,
                  expires: 7,
                });
              }
            });
            // 获取用户信息
            userInfo().then((res) => {
              if (res.success) {
                // 避免超过大小限制
                delete res.result.permissions;
                let roles = [];
                res.result.roles.forEach((e) => {
                  roles.push(e.name);
                });
                delete res.result.roles;
                this.setStore("roles", roles);
                // 保存7天
                Cookies.set("userInfo", JSON.stringify(res.result), {
                  expires: 7,
                });
                this.setStore("userInfo", res.result);
                this.$store.commit("setUserInfo", res.result);
                // 加载菜单
                util.initRouter(this);
                this.$router.push({
                  name: "home_index",
                });
              } else {
                this.socialLogining = false;
                this.$Message.error("获取登录用户信息失败");
              }
            });
          } else {
            this.socialLogining = false;
            this.$Message.error("使用第三方账号登录失败");
          }
        });
      } else {
        this.socialLogining = false;
      }
    },
    handleDropDown(v) {
      if (v == "test") {
        this.test();
      } else if (v == "resetByMobile") {
        this.$router.push({
          name: "reset",
        });
      } else if (v == "resetByEmail") {
        this.$router.push({
          name: "reset",
          query: {
            type: "1",
          },
        });
      }
    },
    showNotice() {
      getNotice().then((res) => {
        if (res.success) {
          if (!res.result) {
            return;
          }
          let data = res.result;
          if (
            data.open &&
            (data.title || data.content) &&
            data.position == "LOGIN"
          ) {
            this.$Notice.info({
              title: data.title,
              desc: data.content,
              duration: data.duration,
            });
          }
        }
      });
    },
    test() {
      this.$Notice.info({
        title: "测试体验账号",
        desc: "账号：test或test2<br>密码：123456",
      });
    },
  },
  mounted() {
    this.showNotice();
    this.relatedLogin();
    this.getCaptchaImg();
  },
};
</script>

<style lang="less">
@import "./login.less";
</style>
