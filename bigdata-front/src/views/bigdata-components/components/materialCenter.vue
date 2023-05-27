<template>
  <div>
    <Divider class="component-blue" orientation="left">素材中心</Divider>
    <Button @click="showCenter1 = true">打开图片素材中心</Button>
    <br />
    <br />
    <Button @click="showCenter2 = true">打开视频素材中心</Button>
    <br />
    <br />
    {{ fileInfo }}
    <materialCenter v-model="showCenter1" multiple @on-change="selectFile"/>
    <materialCenter v-model="showCenter2" @on-change="selectFile" mode="视频" videoId="videoTest"/>
    <h3 class="component-article">基础用法</h3>
    基本用法，使用
    <code>v-model</code>
    实现数据的双向绑定（组件显示）。<br />
    <h3 class="component-article">props</h3>
    <Table
      :columns="props"
      :data="data1"
      border
      size="small"
      width="1000"
    ></Table>
    <h3 class="component-article">events</h3>
    <Table
      :columns="events"
      :data="data2"
      border
      size="small"
      width="1000"
    ></Table>
  </div>
</template>
<script>
import { props, events, methods } from "./columns";
import materialCenter from "@/views/my-components/bigdata/material-center";
export default {
  components: {
    materialCenter,
  },
  data() {
    return {
      showCenter1: false,
      showCenter2: false,
      fileInfo: [],
      props: props,
      events: events,
      methods: methods,
      data1: [
        {
          name: "value",
          desc: "绑定的对象Object或数组Array，可使用 v-model 双向绑定。默认单个上传时只接受单个对象，多个上传时只接受数组",
          type: "Object | Array",
          value: "-",
        },
        {
          name: "mode",
          desc: "素材选择模式，可选值为 图片、视频",
          type: "String",
          value: "图片",
        },
        {
          name: "multiple",
          desc: "是否选开启多个文件上传，默认false",
          type: "Boolean",
          value: "false",
        },
        {
          name: "readonly",
          desc: "是否选允许对文件进行编辑、删除",
          type: "Boolean",
          value: "true",
        },
        {
          name: "showMenus",
          desc: "是否显示文件下方操作按钮",
          type: "Boolean",
          value: "true",
        },
        {
          name: "showUpload",
          desc: "是否显示上传按钮",
          type: "Boolean",
          value: "true",
        },
        {
          name: "showInput",
          desc: "是否显示网络链接上传按钮",
          type: "Boolean",
          value: "true",
        },
        {
          name: "icon",
          desc: "设置上传按钮图标",
          type: "String",
          value: "ios-cloud-upload-outline",
        },
        {
          name: "acceptImg",
          desc: "当mode为图片模式时接受上传的文件类型，等同<input>标签的accept属性",
          type: "String",
          value: ".jpg, .jpeg, .png, .gif",
        },
        {
          name: "acceptVideo",
          desc: "当mode为视频模式时接受上传的文件类型，等同<input>标签的accept属性",
          type: "String",
          value: ".mp4, .wmv, .avi, .mov, .flv, .mkv",
        },
        {
          name: "maxSize",
          desc: "单个文件最大限制大小（单位Mb）",
          type: "Number",
          value: "5",
        },
        {
          name: "maxlength",
          desc: "设置文件链接输入框最大输入长度",
          type: "Number",
          value: "250",
        }
      ],
      data2: [
        {
          name: "on-change",
          type: "返回选择文件对象信息",
          value:
            "默认单选时返回选择的单个文件url，如'http://3.png'；" +
            "当开启多选时，返回选择的多个文件对象数组Array，如['http://1.png','http://2.png']",
        },
      ],
    };
  },
  methods: {
    selectFile(v) {
      this.fileInfo = v;
    },
  },
  mounted() {},
};
</script>