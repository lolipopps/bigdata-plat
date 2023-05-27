<template>
  <div id="main" class="app-main">
    <router-view></router-view>
  </div>
</template>

<script>
export default {
  data() {
    return {};
  },
  computed: {
    mainTheme() {
      return this.$store.state.theme.theme.mainTheme;
    },
  },
  mounted() {
    // 主题预设
    this.changeMode();
  },
  beforeDestroy() {},
  methods: {
    changeMode() {
      let v = this.mainTheme;
      if (v == "darkMode") {
        document.getElementsByTagName("body")[0].className = "darkMode";
      } else if (v == "weakMode") {
        document.getElementsByTagName("body")[0].className = "weakMode";
      } else {
        document.getElementsByTagName("body")[0].className = "";
      }
    },
  },
  watch: {
    mainTheme() {
      this.changeMode();
    },
  },
};
</script>

<style lang="less">
@import "@/styles/common.less";

</style>
