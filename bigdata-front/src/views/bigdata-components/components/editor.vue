<template>
  <div>
    <Divider class="component-blue" orientation="left"
      >wangEditor 富文本编辑器</Divider
    >
    <Alert type="info" show-icon>
      基于
      <a href="http://www.wangeditor.com" target="_blank">wangEditor v5</a>
      封装，已配置好图片上传(上传至bigdata文件服务或Base64)、视频上传；扩展编辑HTML、附件上传、@用户、暗黑主题等。
    </Alert>
    <Tabs v-model="tabName">
      <TabPane label="素材中心上传" name="material"> </TabPane>
      <TabPane label="直接上传" name="direct"> </TabPane>
      <TabPane label="附件上传" name="attach"> </TabPane>
      <TabPane label="@用户" name="mention"> </TabPane>
      <TabPane label="暗黑主题" name="dark"> </TabPane>
    </Tabs>
    <wangEditor
      id="editor-material"
      v-model="materialData"
      material
      v-show="tabName == 'material'"
    ></wangEditor>
    <wangEditor
      id="editor-direct"
      v-model="uploadData"
      v-show="tabName == 'direct'"
    ></wangEditor>
    <wangEditor
      id="editor-attachment"
      v-model="attachData"
      uploadAttachment
      v-show="tabName == 'attach'"
    ></wangEditor>
    <wangEditor
      id="editor-mention"
      v-model="mentionData"
      mention
      v-show="tabName == 'mention'"
    ></wangEditor>
    <wangEditor id="editor-dark" dark v-show="tabName == 'dark'"></wangEditor>
    <h3 class="component-article">样式冲突</h3>
    在 <code>FormItem</code> 中使用时，建议在该标签上加上
    <code>class="form-noheight"</code>。
    <h3 class="component-article">基础用法</h3>
    使用
    <code>v-model</code>
    实现数据的双向绑定，赋值时外层需包含一个HTML标签（编辑HTML代码时同需注意）。单页面同时使用两个及以上该组件时，需设定不同的id值加以区分。
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
    <h3 class="component-article">methods</h3>
    <Table
      :columns="methods"
      :data="data3"
      border
      size="small"
      width="1000"
    ></Table>
  </div>
</template>
<script>
import { props, events, methods } from "./columns";
import wangEditor from "@/views/my-components/bigdata/editor";
export default {
  components: {
    wangEditor,
  },
  data() {
    return {
      tabName: "material",
      props: props,
      events: events,
      methods: methods,
      materialData: "<p>点击图片/视频按钮可通过素材中心上传</p>",
      uploadData: "<p>点击图片/视频按钮可直接选择文件上传</p>",
      attachData:
        '<p>附件点击下载：<span data-w-e-type="attachment" data-w-e-is-void data-w-e-is-inline data-link="http://img.t.sinajs.cn/t4/appstyle/expression/ext/normal/a1/2018new_doge02_org.png" data-fileName="doge.png">doge.png</span></p>',
      mentionData: "<p>输入@可选择指定提醒用户</p>",
      data1: [
        {
          name: "value",
          desc: "绑定的值，可使用 v-model 双向绑定（赋值时外层需包含一个HTML标签）",
          type: "String",
          value: "空",
        },
        {
          name: "id",
          desc: "富文本编辑器的id值，用于绑定富文本编辑器，当同时使用两个及以上该组件时，需设定不同的id值加以区分",
          type: "String",
          value: "editor",
        },
        {
          name: "height",
          desc: "编辑器的高度，默认为300，单位px",
          type: "Number | String",
          value: "360",
        },
        {
          name: "placeholder",
          desc: "占位文本",
          type: "String",
          value: "在这里输入内容",
        },
        {
          name: "zIndex",
          desc: "编辑器的z-index层级（工具栏和内容输入区域），默认为1",
          type: "Number",
          value: "1",
        },
        {
          name: "dark",
          desc: "是否启用暗黑主题模式",
          type: "Boolean",
          value: "false",
        },
        {
          name: "border",
          desc: "是否显示边框（仅在明亮模式下显示）",
          type: "Boolean",
          value: "true",
        },
        {
          name: "material",
          desc: "是否启用素材中心上传图片、视频",
          type: "Boolean",
          value: "false",
        },
        {
          name: "maxSize",
          desc: "启用素材中心时，单个上传文件最大限制大小（单位Mb）",
          type: "Number",
          value: "5",
        },
        {
          name: "uploadPic",
          desc: "是否开启上传图片功能",
          type: "Boolean",
          value: "true",
        },
        {
          name: "base64",
          desc: "是否使用base64存储图片，默认false上传至bigdata配置的文件存储服务中，不推荐使用base64存储",
          type: "Boolean",
          value: "false",
        },
        {
          name: "uploadImgTimeout",
          desc: "图片上传超时时间，单位毫秒，默认为10秒",
          type: "Number",
          value: "10000",
        },
        {
          name: "uploadVideo",
          desc: "是否开启上传视频功能",
          type: "Boolean",
          value: "true",
        },
        {
          name: "uploadVideoTimeout",
          desc: "视频上传超时时间，单位毫秒，默认为30秒",
          type: "Number",
          value: "30000",
        },
        {
          name: "uploadAttachment",
          desc: "是否开启上传附件功能",
          type: "Boolean",
          value: "false",
        },
        {
          name: "uploadAttachTimeout",
          desc: "视频上传超时时间，单位毫秒，默认为20秒",
          type: "Number",
          value: "20000",
        },
        {
          name: "expandHtml",
          desc: "是否显示顶部扩展 编辑Html代码 按钮",
          type: "Boolean",
          value: "true",
        },
        {
          name: "mention",
          desc: "是否启用@用户功能",
          type: "Boolean",
          value: "false",
        },
        {
          name: "showFontFamily",
          desc: "是否显示 设置字体 选择菜单",
          type: "Boolean",
          value: "false",
        },
        {
          name: "showLineHeight",
          desc: "是否显示 设置行高 选择菜单",
          type: "Boolean",
          value: "false",
        },
      ],
      data2: [
        {
          name: "on-change",
          type: "返回富文本编辑器内容",
          value: "value（富文本编辑器内容）",
        },
      ],
      data3: [
        {
          name: "setData",
          type: "设置富文本编辑器内容（外层需包含一个HTML标签）",
          value: "你要传入的内容，示例 setData(‘<p>data</p>’)",
        },
      ],
    };
  },
  methods: {},
  mounted() {},
};
</script>