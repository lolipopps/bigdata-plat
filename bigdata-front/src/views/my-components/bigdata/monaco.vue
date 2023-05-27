<template>
  <div :id="`${id}-monaco-editor-content`" style="position: relative">
    <div
      :class="{ 'header-dark': themeDark, 'monaco-header': true }"
      v-if="showHeader"
    >
      <div :class="{ 'title-dark': themeDark, title: true }">
        {{ showTitle ? title : "" }}
      </div>
      <div class="icons">
        <div
          :class="{ 'btn-dark': themeDark, btn: true }"
          @click="undo"
          v-if="showUndo"
        >
          <Icon type="ios-undo" class="icon"></Icon>
          撤销
        </div>
        <div
          :class="{ 'btn-dark': themeDark, btn: true }"
          @click="redo"
          v-if="showRedo"
        >
          <Icon type="ios-redo" class="icon l"></Icon>
          恢复
        </div>
        <div
          :class="{ 'btn-dark': themeDark, btn: true }"
          @click="copy"
          v-if="showCopy"
        >
          <Icon type="md-copy" class="icon l"></Icon>
          复制
        </div>
        <div
          :class="{ 'btn-dark': themeDark, btn: true }"
          @click="download"
          v-if="showDownload"
        >
          <Icon type="md-download" class="icon l"></Icon>
          下载
        </div>
        <div
          :class="{ 'btn-dark': themeDark, btn: true }"
          @click="format"
          v-if="showFormat"
        >
          <Icon type="md-color-palette" class="icon l"></Icon>
          美化排版
        </div>
        <div
          :class="{ 'btn-dark': themeDark, btn: true }"
          @click="find"
          v-if="showSearch"
        >
          <Icon type="ios-search" class="icon l"></Icon>
          搜索
        </div>
        <div
          :class="{ 'btn-dark': themeDark, 'btn l': true }"
          @click="fullscreen"
          v-if="showFullscreen"
        >
          <Icon
            :type="openFull ? 'md-contract' : 'md-expand'"
            class="icon"
          ></Icon>
          {{ openFull ? "退出全屏" : "全屏" }}
        </div>
        <div
          :class="{ 'btn-dark': themeDark, 'btn l': true }"
          @click="changeTheme"
          v-if="showTheme"
        >
          <Icon :type="themeDark ? 'md-sunny' : 'md-moon'" class="icon"></Icon>
          {{ themeDark ? "明亮主题" : "暗黑主题" }}
        </div>
      </div>
    </div>
    <div :id="id" :style="{ overflow: 'hidden', height: realHeight }"></div>
  </div>
</template>
<script>
// import * as monaco from "monaco-editor";
import FileSaver from "file-saver";
export default {
  name: "monaco",
  props: {
    id: {
      type: String,
      default: "monaco",
    },
    height: {
      type: [Number, String],
      default: "500px",
    },
    value: {
      type: String,
      default: "",
    },
    language: String,
    lineNumbers: {
      type: String,
      default: "on",
    },
    readOnly: {
      type: Boolean,
      default: false,
    },
    cursorStyle: {
      type: String,
      default: "line",
    },
    fontSize: {
      type: Number,
      default: 14,
    },
    contextmenu: {
      type: Boolean,
      default: true,
    },
    defaultTheme: {
      type: String,
      default: "vs",
    },
    autoFormat: {
      type: Boolean,
      default: false,
    },
    showHeader: {
      type: Boolean,
      default: true,
    },
    title: {
      type: String,
    },
    showTitle: {
      type: Boolean,
      default: true,
    },
    showUndo: {
      type: Boolean,
      default: true,
    },
    showRedo: {
      type: Boolean,
      default: true,
    },
    showCopy: {
      type: Boolean,
      default: true,
    },
    showDownload: {
      type: Boolean,
      default: true,
    },
    downloadSuffix: {
      type: String,
      default: ".txt",
    },
    showFormat: {
      type: Boolean,
      default: true,
    },
    showSearch: {
      type: Boolean,
      default: true,
    },
    showFullscreen: {
      type: Boolean,
      default: true,
    },
    showTheme: {
      type: Boolean,
      default: true,
    },
  },
  data() {
    return {
      data: this.value,
      monacoEditor: null,
      themeDark: this.defaultTheme == "vs-dark" ? true : false,
      theme: this.defaultTheme,
      realHeight: this.height,
      openFull: false,
    };
  },
  methods: {
    init() {
      this.monacoEditor = monaco.editor.create(
        document.getElementById(this.id),
        {
          value: this.value,
          language: this.language,
          theme: this.theme, // 主题'vs' (default), 'vs-dark', 'hc-black'
          lineNumbers: this.lineNumbers, // 显示行号
          readOnly: this.readOnly, // 是否只读
          cursorStyle: this.cursorStyle, // 光标
          fontSize: this.fontSize, // 字体大小
          contextmenu: this.contextmenu, // 右键菜单
          formatOnPaste: true,
          formatOnType: true,
        }
      );
      // 格式化代码
      if (this.autoFormat) {
        setTimeout(() => {
          if (!this.monacoEditor) {
            return;
          }
          this.monacoEditor.getAction(["editor.action.formatDocument"]).run();
        }, 200);
      }
      // 监听事件
      this.monacoEditor.onDidChangeModelContent((e) => {
        let v = this.monacoEditor.getValue();
        this.data = v;
        this.$emit("input", this.data);
        this.$emit("on-change", this.data);
      });
    },
    changeTheme() {
      this.themeDark = !this.themeDark;
      let theme = "vs";
      if (this.themeDark) {
        theme = "vs-dark";
      }
      this.monacoEditor.updateOptions({
        theme: theme,
      });
    },
    setData(value) {
      if (!this.monacoEditor) {
        this.init();
      }
      if (value != this.data) {
        this.data = value;
        this.monacoEditor.setValue(this.data);
        // 格式化代码
        if (this.autoFormat) {
          this.monacoEditor.getAction(["editor.action.formatDocument"])._run();
        }
        this.$emit("input", this.data);
        this.$emit("on-change", this.data);
      }
    },
    focus() {
      this.monacoEditor.focus();
    },
    layout() {
      this.realHeight = this.height;
      setTimeout(() => {
        this.monacoEditor.layout();
      }, 10);
    },
    undo() {
      this.monacoEditor.getModel()?.undo();
    },
    redo() {
      this.monacoEditor.getModel()?.redo();
    },
    copy() {
      this.$copyText(this.data).then((e) => {
        this.$Message.success("复制成功");
      });
    },
    download() {
      var blob = new Blob([this.data], {
        type: "text/plain;charset=utf-8",
      });
      let title = this.title;
      if (!title) {
        title = "文件";
      }
      title += this.downloadSuffix;
      FileSaver.saveAs(blob, title);
    },
    format() {
      this.monacoEditor.getAction(["editor.action.formatDocument"])._run();
    },
    find() {
      this.monacoEditor.getAction(["actions.find"])._run();
    },
    fullscreen() {
      let main = document.getElementById(`${this.id}-monaco-editor-content`);
      this.openFull = !this.openFull;
      if (this.openFull) {
        this.realHeight =
          Number(document.body.clientHeight - (this.showHeader ? 40 : 0)) +
          "px";
        main.classList.add("editor-fullscreen");
        setTimeout(() => {
          this.monacoEditor.layout();
        }, 10);
      } else {
        this.realHeight = this.height;
        main.classList.remove("editor-fullscreen");
        setTimeout(() => {
          this.monacoEditor.layout();
        }, 10);
      }
    },
  },
  beforeDestroy() {
    // 调用销毁 API 对当前编辑器实例进行销毁
    if (this.monacoEditor != null) {
      this.monacoEditor.dispose();
    }
  },
  watch: {
    value(val) {
      this.setData(val);
    },
  },
  mounted() {
    this.$nextTick(() => {
      this.init();
    });
  },
};
</script>
<style lang="less" scoped>
.monaco-header {
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #f7f8fa;
  padding: 0 16px;
  .title {
    color: #848b96;
    font-weight: 500;
  }
  .icons {
    display: flex;
    align-items: center;
    .btn {
      cursor: pointer;
      height: 100%;
      color: #848b96;
      transition: all 0.2s ease;
      .icon {
        margin-right: 3px;
      }
      &:hover {
        color: #afb4bd;
      }
    }
    .l {
      margin-left: 14px;
    }
  }
}
.header-dark {
  background: #282a2e;
  .title-dark {
    color: #84888e;
  }
  .btn-dark {
    color: #84888e !important;
    &:hover {
      color: #afb4bd !important;
    }
  }
}
.editor-fullscreen {
  position: fixed !important;
  top: 0;
  left: 0;
  width: 100% !important;
  height: 100% !important;
  z-index: 1000;
}
</style>