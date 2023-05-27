<template>
  <div class="dashboard-page2">
    <Row :gutter="20">
      <Col :xs="24" :sm="24" :md="12" :lg="6">
        <cardArea class="margin" />
      </Col>
      <Col :xs="24" :sm="24" :md="12" :lg="6">
        <cardAreaFill class="margin" />
      </Col>
      <Col :xs="24" :sm="24" :md="12" :lg="6">
        <cardLine />
      </Col>
      <Col :xs="24" :sm="24" :md="12" :lg="6">
        <cardProgress class="margin" />
      </Col>
    </Row>
    <Row :gutter="20">
      <Col :xs="24" :sm="24" :lg="24" :xl="12">
        <lineChart class="margin" />
      </Col>
      <Col :xs="24" :sm="24" :lg="24" :xl="12">
        <bubble class="margin" />
      </Col>
    </Row>
    <Row :gutter="20">
      <Col :xs="24" :sm="24" :lg="24" :xl="18">
        <mapCard class="margin" />
      </Col>
      <Col :xs="24" :sm="24" :lg="24" :xl="6">
        <activity class="margin" />
      </Col>
    </Row>
  </div>
</template>

<script>
import cardArea from "./components/cardArea.vue";
import cardAreaFill from "./components/cardAreaFill.vue";
import cardLine from "./components/cardLine.vue";
import cardProgress from "./components/cardProgress.vue";
import lineChart from "./components/line.vue";
import bubble from "./components/bubble.vue";
import mapCard from "./components/mapCard.vue";
import activity from "./components/activity.vue";
export default {
  name: "dashboard-2",
  components: {
    cardArea,
    cardAreaFill,
    cardLine,
    cardProgress,
    lineChart,
    bubble,
    mapCard,
    activity,
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
.dashboard-page2 {
  .margin {
    margin-bottom: 20px;
  }
}
</style>