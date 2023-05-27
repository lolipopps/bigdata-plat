<template>
  <div class="relate">
    <Row
      type="flex"
      justify="center"
      align="middle"
      @keydown.enter.native="showCaptcha"
      style="height: 100%"
    >
      <Col class="content">
        <div>
          <Header />
          <div v-if="!relateLoading">
            <Tabs value="1">
              <TabPane :label="$t('relateTitle')" name="1" icon="md-person-add">
                <Form
                  ref="form"
                  :model="form"
                  :rules="rules"
                  class="form"
                >
                  <FormItem prop="username">
                    <Input
                      v-model="form.username"
                      prefix="ios-person-outline"
                      size="large"
                      clearable
                      placeholder="请输入登录账号"
                      autocomplete="off"
                    />
                  </FormItem>
                  <FormItem prop="password">
                    <Input
                      type="password"
                      v-model="form.password"
                      prefix="ios-lock-outline"
                      size="large"
                      clearable
                      placeholder="请输入密码"
                      autocomplete="off"
                    />
                  </FormItem>
                </Form>
              </TabPane>
            </Tabs>
            <Row>
              <Button
                type="primary"
                size="large"
                :loading="loading"
                @click="showCaptcha"
                long
              >
                <span v-if="!loading">{{ $t("relate") }}</span>
                <span v-else>{{ $t("relating") }}</span>
              </Button>
            </Row>
            <Row type="flex" justify="space-between" class="other-thing">
              <router-link to="/reset" class="back">{{
                $t("forgetPass")
              }}</router-link>
              <router-link to="/register" class="back">
                {{ $t("registerNow") }}
              </router-link>
            </Row>
          </div>
          <div v-if="relateLoading">
            <RectLoading />
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
      @on-sumbit="submitRelate"
    />
  </div>
</template>

<script>
import Cookies from "js-cookie";
import {
  relate,
  userInfo,
  getJWT,
  getOtherSet,
} from "@/api/index";
import util from "@/libs/util.js";
import Header from "@/views/main-components/header";
import Footer from "@/views/main-components/footer";
import LangSwitch from "@/views/main-components/lang-switch";
import RectLoading from "@/views/my-components/bigdata/rect-loading";
import ImgCaptcha from "@/views/my-components/bigdata/img-captcha";
export default {
  components: {
    LangSwitch,
    Header,
    Footer,
    RectLoading,
    ImgCaptcha
  },
  data() {
    return {
      captchaModal: false,
      relateLoading: true,
      loading: false,
      form: {
        isLogin: false,
        username: "",
        password: "",
        socialType: null,
        id: "",
        captchaId: "",
        code: "",
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
      },
    };
  },
  methods: {
    submitRelate() {
      if (!this.form.id) {
        this.$Message.error("参数非法");
        return;
      }
      this.$refs.form.validate((valid) => {
        if (valid) {
          this.loading = true;
          this.form.isLogin = false;
          relate(this.form).then((res) => {
            if (res.success) {
              // 获取JWT
              getJWT({ JWTKey: res.result }).then((res) => {
                if (res.success) {
                  this.$Message.success("绑定成功");
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
                      this.loading = false;
                    }
                  });
                } else {
                  this.loading = false;
                }
              });
            } else {
              this.loading = false;
            }
          });
        }
      });
    },
    relateDirect() {
      // 已登录 直接绑定
      this.form.isLogin = true;
      relate(this.form).then((res) => {
        if (res.success) {
          this.$Message.success("绑定成功");
        }
        // 返回个人中心
        this.$router.push({
          name: "ownspace_index",
          query: {
            type: "social",
          },
        });
      });
    },
    showCaptcha() {
      this.$refs.form.validate((valid) => {
        if (valid) {
          this.captchaModal = true;
        }
      });
    },
  },
  mounted() {
    let q = this.$route.query;
    this.form.socialType = q.socialType;
    this.form.id = q.id;
    let userInfo = Cookies.get("userInfo");
    if (!userInfo) {
      this.relateLoading = false;
    }
  },
};
</script>

<style lang="less">
@import "./relate.less";
</style>
