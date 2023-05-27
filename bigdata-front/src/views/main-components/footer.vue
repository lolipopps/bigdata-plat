<template>
  <div class="foot">
    <Row type="flex" justify="space-around" class="foot-item">

      <a class="item" href="http://xpay.bigdata.cn/pay?bigdata" target="_blank">{{
        $t("fullVersion")
      }}</a>

    </Row>
  </div>
</template>

<script>
export default {
  name: "bigdata-footer",
};
</script>

<style lang="less" scoped>
.foot {
  width: 100%;
  margin: 48px 0 24px;
  color: rgba(0, 0, 0, 0.45);
  font-size: 14px;
  .foot-item {
    margin: 0 auto;
    margin-bottom: 1vh;
    width: 60%;
    .item {
      color: rgba(0, 0, 0, 0.45);
    }
    :hover {
      color: rgba(0, 0, 0, 0.65);
    }
  }
}
</style>
