<template>
  <div style="display: inline-block">
    <Menu
      :theme="theme"
      mode="horizontal"
      :active-name="currNav"
      @on-select="selectNav"
    >
      <MenuItem
        v-for="(item, i) in navList.slice(0, sliceNum)"
        :key="i"
        :name="item.name"
      >
        <Badge :dot="item.component == 'hot'" :offset="[20, 0]">
          <XIcon :type="item.icon" v-if="showIcon" />
          {{ itemTitle(item) }}
        </Badge>
      </MenuItem>
      <Submenu name="sub" v-if="navList.length > sliceNum">
        <template slot="title">{{ $t("more") }}</template>
        <MenuItem
          v-for="(item, i) in navList.slice(sliceNum, navList.length)"
          :key="i"
          :name="item.name"
        >
          <Badge :dot="item.component == 'hot'">
            <XIcon
              :type="item.icon"
              v-if="showIcon"
              style="margin-right: 6px"
            />
            {{ itemTitle(item) }}
          </Badge>
        </MenuItem>
      </Submenu>
    </Menu>
  </div>
</template>

<script>
import apps from "./apps";
export default {
  name: "nav-app",
  components: {
    apps,
  },
  props: {
    navList: Array,
    currNav: String,
    showIcon: {
      type: Boolean,
      default: true,
    },
    sliceNum: {
      type: Number,
      default: 3,
    },
    theme: {
      type: String,
      default: "light",
    },
  },
  data() {
    return {};
  },
  methods: {
    init() {},
    itemTitle(item) {
      if (item.localize && item.i18n) {
        return this.$t(item.i18n);
      } else {
        return item.title;
      }
    },
    selectNav(v) {
      this.$emit("on-click", v);
    },
  },
  mounted() {
    this.init();
  },
  watch: {},
};
</script>

<style lang="less">
</style>