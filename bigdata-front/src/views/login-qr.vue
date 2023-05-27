<template>
  <div class="login">
    <Row type="flex" justify="center" align="middle" style="height: 100%">
      <Col class="content">
        <div>
          <Header />

          <div style="position: relative">
            <Tabs>
              <TabPane label="扫码登录" name="qr" icon="md-qr-scanner">
              </TabPane>
            </Tabs>
            <Tooltip
              content="bigdata账号登录"
              placement="right"
              class="qr block-tool"
            >
              <router-link to="/login">
                <XIcon type="iconfont icon-zhanghaodenglu" size="30" />
              </router-link>
            </Tooltip>
          </div>
          <div class="qr-content">
            <div class="code-content">
              <img :src="qrUrl" class="qr-img" />
              <div
                class="qr-status"
                v-show="qrStatus != '0' && qrStatus != '2'"
              >
                <span v-show="qrStatus == '-2'">二维码已失效</span>
                <span v-show="qrStatus == '-1'">您已取消此次登录</span>
                <Icon
                  type="md-checkmark-circle"
                  color="#19be6b"
                  size="34"
                  v-show="qrStatus == '1'"
                />
                <span v-show="qrStatus == '1'" class="success">扫码成功</span>
                <span v-show="qrStatus == '1'">请在手机上确认登录</span>
                <Button
                  size="small"
                  type="primary"
                  class="refresh"
                  @click="getQRCode"
                  v-show="qrStatus != '1'"
                  >点击刷新</Button
                >
              </div>
            </div>
            <div class="qr-tip">请使用 bigdata App 扫描二维码登录</div>
          </div>

          <Row type="flex" justify="space-between" class="other-login">
            <div class="other-way icons"></div>
            <router-link to="/register">
              <a class="forget-pass">{{ $t("registerAccount") }}</a>
            </router-link>
          </Row>
        </div>
        <Footer />
      </Col>
      <LangSwitch />
    </Row>
  </div>
</template>

<script>
import {
  getLoginQRCode,
  checkQRStatus,
  userInfo,
  getOtherSet,
} from "@/api/index";

import Header from "@/views/main-components/header";
import Footer from "@/views/main-components/footer";
import LangSwitch from "@/views/main-components/lang-switch";

import util from "@/libs/util.js";
import QRCode from "qrcode";
import Cookies from 'js-cookie';
export default {
  components: {
    LangSwitch,
    Header,
    Footer,
  },
  data() {
    return {
      loading: false,
      checkToken: "",
      // 二维码状态 初始为0
      qrStatus: "0",
      qrUrl: "",
      checkInterval: null,
    };
  },
  methods: {
    init() {
      this.getQRCode();
    },
    getQRCode() {
      getLoginQRCode().then((res) => {
        if (res.success) {
          this.qrStatus = "0";
          let url = res.result;
          this.generateQR(url);
          this.checkToken = url.substring(url.indexOf("=") + 1, url.length);
          this.checkLoginQRStatus();
        }
      });
    },
    generateQR(v) {
      QRCode.toDataURL(
        v,
        {
          margin: 0,
        },
        (err, url) => {
          if (err) {
            this.$Message.error("生成二维码图片失败");
          }

          this.qrUrl = url;
        }
      );
    },
    checkLoginQRStatus() {
      // 3秒检测一次二维码状态
      if (this.checkInterval) {
        clearInterval(this.checkInterval);
      }
      this.checkInterval = setInterval(() => {
        checkQRStatus(this.checkToken).then((res) => {
          if (res.success) {
            this.qrStatus = res.result.status;
            if (this.qrStatus < "0") {
              clearInterval(this.checkInterval);
            }
            if (this.qrStatus == "2") {
              let accessToken = res.result.accessToken;
              this.$Message.success("扫码登录成功");
              this.setStore("accessToken", accessToken);
              this.afterLogin(accessToken);
            }
          }
        });
      }, 3000);
    },
    afterLogin(accessToken) {
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
          this.setStore("saveLogin", this.saveLogin);
          if (this.saveLogin) {
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
    submitLogin() {},
  },
  mounted() {
    this.init();
  },
  beforeDestroy() {
    if (this.checkInterval) {
      clearInterval(this.checkInterval);
    }
  },
};
</script>

<style lang="less">
@import "./login.less";
.qr-content {
  height: 292px;
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  .code-content {
    position: relative;
    box-sizing: border-box;
    width: 180px;
    height: 180px;
    padding: 7px;
    margin: 0 auto;
    background: #fff;
    border: 1px solid #e5e5e5;
    transition: transform 0.3s;
    .qr-img {
      width: 164px;
      height: 164px;
    }
    .qr-status {
      opacity: 1;
      position: absolute;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      width: 100%;
      height: 100%;
      left: 0;
      top: 0;
      background: hsla(0, 0%, 100%, 0.9);
      font-size: 14px;
      color: #323233;
      font-weight: 500;
      transition: opacity 0.3s;
      .success {
        color: #19be6b;
        font-size: 16px;
        margin: 5px 0;
      }
      .refresh {
        margin-top: 15px;
      }
    }
  }
  .qr-tip {
    margin-top: 30px;
  }
}
</style>
