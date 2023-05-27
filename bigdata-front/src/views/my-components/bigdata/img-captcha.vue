<template>
  <div>
    <Modal title="图片验证码" v-model="showModal">
      <Row
        type="flex"
        justify="space-between"
        style="align-items: center; overflow: hidden"
      >
        请输入验证码
        <Input
          v-model="code"
          clearable
          size="large"
          placeholder="请输入图片验证码"
          :maxlength="10"
          style="width: 250px"
          @on-change="changeCode"
        />
        <div class="code-image" style="position: relative; font-size: 12px">
          <Spin v-if="loading" fix></Spin>
          <img
            :src="captchaImg"
            @click="getCaptchaImg"
            alt="加载验证码失败"
            style="width: 120px; cursor: pointer; display: block"
          />
        </div>
      </Row>
      <div slot="footer">
        <Button type="text" @click="showModal = false">取消</Button>
        <Button type="primary" @click="submit">提交</Button>
      </div>
    </Modal>
  </div>
</template>

<script>
import { initCaptcha, drawCodeImage } from "@/api/index";
export default {
  name: "imgCaptcha",
  props: {
    value: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      loading: false,
      showModal: this.value,
      captchaId: "",
      code: "",
      captchaImg: "",
    };
  },
  methods: {
    show() {
      this.getCaptchaImg();
      this.showModal = true;
    },
    getCaptchaImg() {
      this.loading = true;
      initCaptcha().then((res) => {
        this.loading = false;
        if (res.success) {
          this.code = "";
          this.captchaId = res.result;
          this.$emit("on-captchaId", this.captchaId);
          this.captchaImg = drawCodeImage + this.captchaId;
        }
      });
    },
    changeCode(v) {
      this.$emit("on-code", this.code);
    },
    submit() {
      if (!this.code) {
        this.$Message.error("请输入验证码");
        return;
      }
      this.$emit("on-sumbit", this.code);
      this.showModal = false;
    },
  },
  watch: {
    value(val) {
      if (val == true) {
        this.show();
      }
    },
    showModal(value) {
      this.$emit("input", value);
    },
  },
};
</script>

<style lang="less">
.code-image {
  .ivu-spin-fix .ivu-spin-main {
    height: 20px;
  }
}
</style>

