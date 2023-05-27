<template>
  <div>
    <Card class="card-info">
      <span class="lstick"></span>
      <div class="info-wrap">
        <img :src="icon" class="icon" />
        <div class="content">
          <h6 class="text">{{ title }}</h6>
          <countUp
            class="mt"
            color="#67757c"
            :countWeight="400"
            countSize="24px"
            :id="id"
            :endVal="value"
          ></countUp>
        </div>
      </div>
    </Card>
  </div>
</template>

<script>
import countUp from "@/views/my-components/widget/countUp";
export default {
  name: "card2",
  components: {
    countUp,
  },
  props: {
    id: {
      type: String,
      default: "countup",
    },
    icon: {
      type: String,
      default: "https://ooo.0o0.ooo/2019/11/22/OSp4jzy5TrLAvM3.png",
    },
    title: {
      type: String,
      default: "-",
    },
    value: {
      type: Number,
      default: 0,
    },
  },
  data() {
    return {};
  },
  methods: {
    init() {},
  },
  mounted() {
    this.init();
  },
};
</script>
<style lang="less" scoped>
.card-info {
  .lstick {
    background: #478ef9;
    width: 2px;
    height: 30px;
    display: inline-block;
    vertical-align: middle;
    position: absolute;
    left: 0;
    top: 38px;
  }
  .info-wrap {
    display: flex;
    align-items: center;
    height: 70px;
    .icon {
      display: inline-block;
      width: 55px;
      margin-right: 1rem;
    }
    .content {
      display: flex;
      flex-direction: column;
      .text {
        color: #99abb4;
        font-size: 0.875rem;
        margin-top: 0.5rem;
      }
      .mt {
        margin-bottom: 0.5rem;
        line-height: 1.2;
      }
    }
  }
}
</style>