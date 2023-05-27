<style lang="less">
@import "./home.less";
</style>

<template>
  <div>
    <!-- <div v-if="!currNav || currNav == 'bigdata'" class="home"> -->

    <div class="home">
      <Row :gutter="10">
        <!-- 左上侧 用户信息及github链接 -->

        <Col :lg="24" :xl="24">
        <Card :padding="0">
          <div class="welcome-card">
            <div class="left">
              <div class="user">
                <Avatar v-if="avatar" :src="avatar" size="60" class="avator-img"></Avatar>
                <Avatar v-else icon="md-person" size="60" class="avator-icon"></Avatar>
                <div class="info">
                  <p class="username">Hi, {{ nickname }} !</p>
                  <p class="welcome">欢迎使用大数据平台</p>
                </div>
              </div>
              <div class="list">
                <div class="p">
                  <div class="dot"></div>
                  您当前所在的部门为：{{ departmentTitle }}
                </div>
                <div class="p">
                  <div class="dot"></div>
                  您当前的用户类型为：{{ userType }}
                </div>
                <div class="p">
                  <div class="dot"></div>
                  本次登录地点：{{ city }}
                </div>
              </div>
            </div>
            <img class="pic" :src="require('@/assets/icon/computer.svg')" />
          </div>
        </Card>
        </Col>

      </Row>

      <Row :gutter="10">
        <Col :xs="24" :sm="24" :md="12" :lg="6" :style="{ marginBottom: '10px' }">
        <card1 id="card5" :bordered="false" :end-val="126778" title="今日新增互动数" backgroundColor="#fff4df" :image="require('@/assets/icon/comment.png')" width="34px" height="34px" />
        </Col>
        <Col :xs="24" :sm="24" :md="12" :lg="6" :style="{ marginBottom: '10px' }">
        <card1 id="card6" :bordered="false" :end-val="68893" backgroundColor="#6993fe" countColor="#fff" icon="md-person-add" iconColor="#fff" titleColor="#fff" title="今日新增用户" />
        </Col>
        <Col :xs="24" :sm="24" :md="12" :lg="6" :style="{ marginBottom: '10px' }">
        <card1 id="card7" :bordered="false" :end-val="count.data4" backgroundColor="#8950fe" countColor="#fff" icon="md-cloud-download" iconColor="#fff" titleColor="#fff" title="今日下载量" />
        </Col>
        <Col :xs="24" :sm="24" :md="12" :lg="6" :style="{ marginBottom: '10px' }">
        <card1 id="card8" :bordered="false" :end-val="13507632434" backgroundColor="#f64e61" countColor="#fff" icon="md-calendar" iconColor="#fff" titleColor="#fff" title="月活" />
        </Col>
      </Row>
      <Row :gutter="10">
        <Col :xs="24" :sm="24" :lg="24" :xl="16" :style="{ marginBottom: '10px' }">
        <visitVolume />
        </Col>
        <Col :xs="24" :sm="24" :lg="24" :xl="8" :style="{ marginBottom: '10px' }">
        <visitSeparation />
        </Col>
      </Row>
    </div>
    <div v-if="currNav == 'app'">
      <Dashboard />
    </div>

    <Modal v-model="showVideo" title="作者亲自制作bigdata炫酷文字快闪宣传片" :styles="{ top: '30px' }" footer-hide width="1000">
      <iframe src="//player.bilibili.com/player.html?aid=30284667&cid=52827707&page=1" scrolling="no" border="0" frameborder="no" framespacing="0" allowfullscreen="true" style="width: 100%; height: 550px"></iframe>
    </Modal>
  </div>
</template>

<script>
import { ipInfo, getNotice } from "@/api/index";
import visitVolume from "./components/visitVolume.vue";
import visitSeparation from "./components/visitSeparation.vue";
import card1 from "@/views/my-components/widget/card1.vue";
import card2 from "./components/card2.vue";
import card3 from "@/views/my-components/widget/card3.vue";
import card4 from "@/views/my-components/widget/card4.vue";
import cardApp from "./components/cardApp.vue";
import Dashboard from "@/views/bigdata-charts/dashboard2/dashboard2.vue";
import Cookies from "js-cookie";
import Gitalk from "gitalk";

export default {
  name: "home",
  components: {
    visitVolume,
    visitSeparation,
    card1,
    card2,
    card3,
    card4,
    cardApp,
    Dashboard,
  },
  data() {
    return {
      showVideo: false,
      count: {
        data1: 5396,
        data2: 68,
        data3: 19305,
        data4: 39503498,
      },
      city: "未知",
      departmentTitle: "无",
      userType: "无",
      time: "",
      price: "...",
    };
  },
  computed: {
    currNav() {
      return this.$store.state.app.currNav;
    },
    nickname() {
      return this.$store.state.user.nickname;
    },
    avatar() {
      return this.$store.state.user.avatar;
    },
  },
  methods: {
    init() {
      let userInfo = this.getUserInfo();
      this.departmentTitle = userInfo.departmentTitle;
      if (userInfo.type == "0") {
        this.userType = "普通用户";
      } else if (userInfo.type == "1") {
        this.userType = "管理员";
      }
      ipInfo().then((res) => {
        if (res.success) {
          this.city = res.result;
        }
      });
      this.time = this.format(new Date(), "yyyy年MM月dd日");
    },
    showNotice() {
      getNotice().then((res) => {
        if (res.success) {
          if (!res.result) {
            return;
          }
          let data = res.result;
          if (
            data.open &&
            (data.title || data.content) &&
            data.position == "HOME"
          ) {
            this.$Notice.info({
              title: data.title,
              desc: data.content,
              duration: data.duration,
            });
          }
        }
      });
    },
  },
  mounted() {
    this.init();
    // 通知
    let noticeFlag = "noticeShowed";
    let notice = Cookies.get(noticeFlag);
    if (notice != noticeFlag) {
      this.showNotice();
      Cookies.set(noticeFlag, noticeFlag);
    }
    // 价格
    AV.init({
      appId: "6Bstbxl4NDU69I77D3nzf61h-gzGzoHsz",
      appKey: "gaFTnYlTul3M8qdiGlbfvoJK",
      serverURL: "https://6bstbxl4.lc-cn-n1-shared.com",
    });
    const query = new AV.Query("Price");
    query.equalTo("objectId", "6080216c2a5bb23590bcaedb");
    query.first().then((e) => {
      this.price = e.attributes.price;
    });
    // Gitalk
    var gitalk = new Gitalk({
      clientID: "a128de2dd7383614273a",
      clientSecret: "a77691ecb662a8303a6c686ae651ae035868da6e",
      repo: "bigdata-comments",
      owner: "Exrick",
      admin: ["Exrick"],
      distractionFreeMode: false, // 遮罩效果
    });
    try {
      gitalk.render("comments");
    } catch (err) { }
    // 宣传视频
    let videoFlag = "videoShowed";
    let bigdataVideo = Cookies.get(videoFlag);
    if (bigdataVideo != videoFlag) {
      this.showVideo = true;
      Cookies.set(videoFlag, videoFlag);
    }
  },
};
</script>
