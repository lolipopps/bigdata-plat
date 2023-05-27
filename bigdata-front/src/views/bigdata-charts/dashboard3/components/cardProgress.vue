<template>
  <div>
    <Card :padding="0" :bordered="false" class="card-progress">
      <div class="info-wrap">
        <div class="title-content">
          <div class="title">销售渠道排行榜</div>
          <div>
            <Dropdown class="sortby" @on-click="sortBy = $event">
              <span>
                {{ sortBy }}
                <Icon type="ios-arrow-down"></Icon>
              </span>
              <DropdownMenu slot="list">
                <DropdownItem name="按年统计">按年统计</DropdownItem>
                <DropdownItem name="按月统计">按月统计</DropdownItem>
                <DropdownItem name="按周统计">按周统计</DropdownItem>
              </DropdownMenu>
            </Dropdown>
          </div>
        </div>
        <div class="progress-content">
          <div class="progress-item">
            <div class="dot" style="background: #5b73e8"></div>
            <div class="title">PC端</div>
            <Progress
              :percent="percent1"
              :stroke-width="6"
              stroke-color="#5b73e8"
              hide-info
              style="width: 80%"
            />
          </div>
          <div class="progress-item">
            <div class="dot" style="background: #23b397"></div>
            <div class="title">iPhone</div>
            <Progress
              :percent="percent2"
              :stroke-width="6"
              stroke-color="#23b397"
              hide-info
              style="width: 80%"
            />
          </div>
          <div class="progress-item">
            <div class="dot" style="background: #56c2d6"></div>
            <div class="title">Android</div>
            <Progress
              :percent="percent3"
              :stroke-width="6"
              stroke-color="#56c2d6"
              hide-info
              style="width: 80%"
            />
          </div>
          <div class="progress-item">
            <div class="dot" style="background: #f8cc6b"></div>
            <div class="title">小程序</div>
            <Progress
              :percent="percent4"
              :stroke-width="6"
              stroke-color="#f8cc6b"
              hide-info
              style="width: 80%"
            />
          </div>
          <div class="progress-item">
            <div class="dot" style="background: #f0643b"></div>
            <div class="title">平板端</div>
            <Progress
              :percent="percent5"
              :stroke-width="6"
              stroke-color="#f0643b"
              hide-info
              style="width: 80%"
            />
          </div>
        </div>
      </div>
    </Card>
  </div>
</template>

<script>
export default {
  name: "card-button",
  components: {},
  props: {},
  data() {
    return {
      percent1: 0,
      percent2: 0,
      percent3: 0,
      percent4: 0,
      percent5: 0,
      sortBy: "按年统计",
    };
  },
  methods: {
    init() {
      setTimeout(() => {
        this.percent1 = 72;
        this.percent2 = 39;
        this.percent3 = 61;
        this.percent4 = 52;
        this.percent5 = 28;
      }, 200);
    },
  },
  mounted() {
    this.init();
  },
};
</script>
<style lang="less" scoped>
.card-progress {
  .info-wrap {
    display: flex;
    flex-direction: column;
    height: 330px;
    padding: 20px;
    .title-content {
      width: 100%;
      display: flex;
      align-items: center;
      justify-content: space-between;
      font-size: 16px;
      font-weight: 600;
      color: #495057;
      margin-bottom: 24px;
      .sortby {
        font-size: 14px;
        cursor: pointer;
        font-weight: 400;
        color: #74788d;
      }
    }
    .progress-content {
      display: flex;
      flex-direction: column;
      justify-content: space-around;
      height: 280px;
      .progress-item {
        display: flex;
        align-items: center;
        .dot {
          width: 5px;
          height: 5px;
          border-radius: 50%;
          margin-right: 16px;
        }
        .title {
          width: 70px;
          margin-right: 5px;
          text-overflow: ellipsis;
          white-space: nowrap;
          overflow: hidden;
        }
      }
    }
  }
}
</style>