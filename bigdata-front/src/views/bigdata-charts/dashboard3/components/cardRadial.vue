<template>
  <div>
    <Card :padding="0" class="card-radial">
      <div class="info-wrap">
        <div class="top">
          <div class="left">
            <countUp
              :id="id"
              :prefix="prefix"
              :suffix="suffix"
              :endVal="value"
              :decimalPlaces="decimalPlaces"
              countSize="22px"
              countWeight="500"
            />
            <h6 class="text">{{ title }}</h6>
          </div>
          <div class="right-chart">
            <apexchart
              type="radialBar"
              height="45"
              width="45"
              :options="chartOptions"
              :series="series"
            />
          </div>
        </div>
        <div class="bottom">
          <Icon
            type="md-arrow-round-up"
            color="#34c38f"
            size="20"
            v-if="status == 'up'"
          />
          <Icon type="md-arrow-round-down" size="20" color="#f46a6a" v-else />
          <span :class="`compare-${status}`">{{ compare }}</span>
          <span class="since">{{ since }}</span>
        </div>
      </div>
    </Card>
  </div>
</template>

<script>
import countUp from "@/views/my-components/widget/countUp.vue";
export default {
  name: "card-radial",
  components: {
    countUp
  },
  props: {
    id: {
      type: String,
      default: "countup",
    },
    title: {
      type: String,
      default: "-",
    },
    value: {
      type: Number,
      default: 0,
    },
    decimalPlaces: {
      type: Number,
      default: 0,
    },
    prefix: String,
    suffix: String,
    status: {
      type: String,
      default: "up",
    },
    compare: String,
    since: {
      type: String,
      default: "与上周相比",
    },
  },
  data() {
    return {
      series: [60],
      chartOptions: {
        colors: ["#5b73e8"],
        chart: {
          sparkline: {
            enabled: true,
          },
        },
        dataLabels: {
          enabled: false,
        },
        plotOptions: {
          radialBar: {
            hollow: {
              margin: 0,
              size: "60%",
            },
            track: {
              margin: 0,
            },
            dataLabels: {
              show: false,
            },
          },
        },
      },
    };
  },
  methods: {
    init() {
    },
  },
  mounted() {
    this.init();
  },
};
</script>
<style lang="less" scoped>
.card-radial {
  .info-wrap {
    display: flex;
    align-items: center;
    flex-direction: column;
    height: 132px;
    padding: 20px;
    .top {
      width: 100%;
      display: flex;
      justify-content: space-between;
      align-items: flex-end;
      .left {
        .mt {
          color: #495057;
          font-size: 22px;
          font-weight: 500;
        }
        .text {
          color: #74788d;
          font-weight: 400;
          font-size: 14px;
        }
      }
    }
    .bottom {
      width: 100%;
      display: flex;
      color: #74788d;
      margin-top: 16px;
      .compare-up {
        color: #34c38f;
        font-size: 15px;
        margin-right: 8px;
        font-weight: 500;
      }
      .compare-down {
        color: #f46a6a;
        font-size: 15px;
        margin-right: 8px;
        font-weight: 500;
      }
      .since {
        color: #74788d;
      }
    }
  }
}
</style>