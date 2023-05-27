<template>
  <div>
    <Divider class="component-blue" orientation="left"
      >Monaco 代码编辑器</Divider
    >
    <Alert type="info" show-icon>
      基于
      <a href="https://github.com/microsoft/monaco-editor" target="_blank"
        >Monaco Editor</a
      >
      封装，已配置好中文，支持v-model双向绑定，属性配置等。
    </Alert>
    <monaco
      language="javascript"
      v-model="data"
      defaultTheme="vs-dark"
      title="编辑器标题"
      height="300px"
      style="width: 1000px"
    />
    <h3 class="component-article">基础用法</h3>
    使用
    <code>v-model</code>
    实现数据的双向绑定。单页面同时使用两个及以上该组件时，需设定不同的id值加以区分。
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
import monaco from "@/views/my-components/bigdata/monaco";
export default {
  components: {
    monaco,
  },
  data() {
    return {
      data: "function hello() {\n\talert('Hello world!');\n}",
      props: props,
      events: events,
      methods: methods,
      data1: [
        {
          name: "value",
          desc: "绑定的值，可使用 v-model 双向绑定",
          type: "String",
          value: "空",
        },
        {
          name: "id",
          desc: "代码编辑器的id值，用于绑定代码编辑器，当同时使用两个及以上该组件时，需设定不同的id值加以区分",
          type: "String",
          value: "monaco",
        },
        {
          name: "height",
          desc: "编辑器的高度，需带单位，默认为500px",
          type: "Number | String",
          value: "500px",
        },
        {
          name: "language",
          desc: "代码语言，支持的语言有：'abap', 'apex', 'azcli', 'bat', 'cameligo', 'clojure', 'coffee', 'cpp', 'csharp', 'csp', 'css', 'dart', 'dockerfile', 'fsharp', 'go', 'graphql', 'handlebars', 'hcl', 'html', 'ini', 'java', 'javascript', 'json', 'julia', 'kotlin', 'less', 'lexon', 'lua', 'markdown', 'mips', 'msdax', 'mysql', 'objective-c', 'pascal', 'pascaligo', 'perl', 'pgsql', 'php', 'postiats', 'powerquery', 'powershell', 'pug', 'python', 'r', 'razor', 'redis', 'redshift', 'restructuredtext', 'ruby', 'rust', 'sb', 'scala', 'scheme', 'scss', 'shell', 'solidity', 'sophia', 'sql', 'st', 'swift', 'systemverilog', 'tcl', 'twig', 'typescript', 'vb', 'xml', 'yaml'",
          type: "String",
          value: "空",
        },
        {
          name: "lineNumbers",
          desc: "是否显示行号，开启'on'，关闭'off'",
          type: "String",
          value: "on",
        },
        {
          name: "readOnly",
          desc: "是否只读（无法编辑）",
          type: "Boolean",
          value: "false",
        },
        {
          name: "cursorStyle",
          desc: "光标样式，可选值有'line' | 'block' | 'underline' | 'line-thin' | 'block-outline' | 'underline-thin'",
          type: "String",
          value: "line",
        },
        {
          name: "fontSize",
          desc: "字体大小",
          type: "Number",
          value: "14",
        },
        {
          name: "contextmenu",
          desc: "是否启用右键菜单",
          type: "Boolean",
          value: "true",
        },
        {
          name: "defaultTheme",
          desc: "默认主题，可选值有'vs' | 'vs-dark'",
          type: "String",
          value: "vs",
        },
        {
          name: "autoFormat",
          desc: "自动格式化",
          type: "Boolean",
          value: "false",
        },
        {
          name: "showHeader",
          desc: "是否显示顶部菜单栏",
          type: "Boolean",
          value: "true",
        },
        {
          name: "title",
          desc: "顶部菜单栏标题（下载文件名）",
          type: "String",
          value: "-",
        },
        {
          name: "showTitle",
          desc: "是否显示顶部菜单栏标题",
          type: "Boolean",
          value: "true",
        },
        {
          name: "showUndo",
          desc: "是否显示顶部菜单栏撤销按钮",
          type: "Boolean",
          value: "true",
        },
        {
          name: "showRedo",
          desc: "是否显示顶部菜单栏恢复按钮",
          type: "Boolean",
          value: "true",
        },
        {
          name: "showCopy",
          desc: "是否显示顶部菜单栏复制按钮",
          type: "Boolean",
          value: "true",
        },
        {
          name: "showDownload",
          desc: "是否显示顶部菜单栏下载按钮",
          type: "Boolean",
          value: "true",
        },
        {
          name: "downloadSuffix",
          desc: "生成下载文件名后缀（文件名为title属性，若缺省则为'文件'）",
          type: "String",
          value: ".txt",
        },
        {
          name: "showFormat",
          desc: "是否显示顶部菜单栏美化排版按钮",
          type: "Boolean",
          value: "true",
        },
        {
          name: "showSearch",
          desc: "是否显示顶部菜单栏搜索按钮",
          type: "Boolean",
          value: "true",
        },
        {
          name: "showFullscreen",
          desc: "是否显示顶部菜单栏全屏按钮",
          type: "Boolean",
          value: "true",
        },
        {
          name: "showTheme",
          desc: "是否显示顶部菜单栏主题切换按钮",
          type: "Boolean",
          value: "true",
        },
      ],
      data2: [
        {
          name: "on-change",
          type: "返回编辑器代码内容",
          value: "value（编辑器代码内容）",
        },
      ],
      data3: [
        {
          name: "setData",
          type: "设置编辑器代码内容",
          value: "你要传入的内容，示例 setData(data)",
        },
        {
          name: "layout",
          type: "自动重新布局编辑器宽度",
          value: "无",
        },
        {
          name: "focus",
          type: "聚焦编辑器",
          value: "无",
        },
      ],
    };
  },
  methods: {
    init() {},
  },
  mounted() {
    this.init();
  },
};
</script>