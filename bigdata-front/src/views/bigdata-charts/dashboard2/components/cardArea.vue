<template>
  <div>
    <Card class="card-area">
      <div class="card-header">
        <span class="title">总销售额</span>
        <Dropdown trigger="click">
          <Icon type="ios-more" size="22" style="cursor: pointer" />
          <DropdownMenu slot="list">
            <DropdownItem>查看详情</DropdownItem>
            <DropdownItem>更多操作</DropdownItem>
          </DropdownMenu>
        </Dropdown>
      </div>
      <div class="value">
        <count-up
          id="dashboard2-1"
          prefix="￥"
          :endVal="126560"
          countSize="30px"
          countWeight="400"
        />
      </div>
      <apexchart
        type="area"
        height="45"
        :options="chartOptions"
        :series="series"
      ></apexchart>
      <div class="today">
        <span class="t">今日销售额</span>
        <span class="v">￥12,423</span>
      </div>
    </Card>
  </div>
</template>

<script>
import countUp from "@/views/my-components/widget/countUp.vue";
var zh = require("../../../../libs/zh.json");
export default {
  name: "cardArea",
  components: {
    countUp,
  },
  props: {},
  data() {
    return {
      series: [
        {
          name: "",
          data: [],
        },
      ],
      chartOptions: {
        colors: ["#ff455f"],
        chart: {
          id: "dashboard2-1",
          type: "area",
          sparkline: {
            enabled: true,
          },
          locales: [zh],
          defaultLocale: "zh",
        },
        dataLabels: {
          enabled: false,
        },
        stroke: {
          width: 2,
          curve: "straight",
        },
        labels: [...Array(12).keys()].map((n) => `2019-11-${n + 1}`),
        yaxis: {
          min: 0,
        },
        xaxis: {
          type: "datetime",
        },
        tooltip: {
          enabled: true,
          x: {
            show: true,
            format: "MMM dd日",
          },
        },
      },
    };
  },
  methods: {
    init() {
      this.series = [
        {
          name: "销售额",
          data: [47, 45, 54, 38, 56, 24, 65, 31, 37, 39, 62, 51],
        },
      ];
    },
  },
  mounted() {
    this.init();
  },
};
</script>
<style lang="less" scoped>
.card-area {
  height: 182px;
  .card-header {
    display: flex;
    height: 22px;
    align-items: center;
    justify-content: space-between;
    color: rgba(0, 0, 0, 0.45);
  }
  .value {
    height: 38px;
    margin-top: 4px;
    margin-bottom: 6px;
    overflow: hidden;
    color: rgba(0, 0, 0, 0.85);
    font-size: 30px;
    line-height: 38px;
    white-space: nowrap;
    text-overflow: ellipsis;
    word-break: break-all;
  }
  .today {
    margin-top: 8px;
    padding-top: 9px;
    border-top: 1px solid #e8e8e8;
    .v {
      margin-left: 8px;
      color: rgba(0, 0, 0, 0.85);
    }
  }
}
</style>