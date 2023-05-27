<template>
  <div class="dashboard-page">
    <Row :gutter="20">
      <Col :xs="24" :sm="24" :lg="8">
        <cardLine class="margin" />
      </Col>
      <Col :xs="24" :sm="24" :lg="16">
        <Row :gutter="20">
          <Col :xs="24" :sm="24" :lg="6">
            <card1 class="margin" />
          </Col>
          <Col :xs="24" :sm="24" :lg="18">
            <card2 class="margin" />
          </Col>
        </Row>
        <Row :gutter="20">
          <Col :xs="24" :sm="24" :lg="12">
            <Row :gutter="20">
              <Col :xs="24" :sm="24">
                <card3 class="margin" />
              </Col>
              <Col :xs="24" :sm="24">
                <Row :gutter="20">
                  <Col :xs="24" :sm="24" :lg="12">
                    <card4
                      id="card4"
                      :bordered="false"
                      :end-val="193"
                      backgroundColor="#6993fe"
                      countColor="#fff"
                      icon="ios-apps"
                      iconColor="#fff"
                      titleColor="#fff"
                      iconSize="26"
                      title="今日新增应用"
                      titleSize="14px"
                      class="margin"
                    />
                  </Col>
                  <Col :xs="24" :sm="24" :lg="12">
                    <card4
                      id="card4-2"
                      :bordered="false"
                      :end-val="863"
                      countColor="#3f4255"
                      icon="md-people"
                      iconColor="#19be6b"
                      titleColor="#b5b5c5"
                      iconSize="34"
                      title="今日新增用户"
                      titleSize="14px"
                      class="margin"
                    />
                  </Col>
                </Row>
              </Col>
            </Row>
          </Col>
          <Col :xs="24" :sm="24" :lg="12">
            <card5 class="margin" />
          </Col>
        </Row>
      </Col>
    </Row>
    <Row :gutter="20">
      <Col :xs="24" :sm="24" :lg="24" :xl="8">
        <cardData class="margin" />
      </Col>
      <Col :xs="24" :sm="24" :lg="24" :xl="16">
        <cardChart class="margin" />
      </Col>
    </Row>
  </div>
</template>

<script>
import cardLine from "./components/cardLine.vue";
import card1 from "./components/card1.vue";
import card2 from "./components/card2.vue";
import card3 from "./components/card3.vue";
import card4 from "@/views/my-components/widget/card1.vue";
import card5 from "./components/card5.vue";
import cardData from "./components/cardData.vue";
import cardChart from "./components/cardChart.vue";
export default {
  name: "dashboard-4",
  components: {
    cardLine,
    card1,
    card2,
    card3,
    card4,
    card5,
    cardData,
    cardChart,
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
.dashboard-page {
  .margin {
    margin-bottom: 20px;
  }
}
</style>