<template>
  <div>
    <Card style="min-height:85vh">
      <div class="sso">
        <img class="icon" src="@/assets/logo-min.png" />
        <h2>
          请点击前往站点
          <a href="http://sso.bigdata.cn" target="_blank">sso.bigdata.cn</a> 进行测试
        </h2>
        <a href="http://sso.bigdata.cn" target="_blank">
          <Button type="primary" style="margin-top:20px;">立即前往另一站点</Button>
        </a>
      </div>
    </Card>
  </div>
</template>

<script>
export default {
  name: "sso",
  data() {
    return {};
  },
  methods: {},
  mounted() {}
};
</script>

<style lang="less">
.sso {
  height: 50vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  .icon {
    width: 100px;
  }
}
</style>
