<template>
  <div :class="{ 'wang-editor': border, 'wang-editor-dark': dark }">
    <div
      :id="`${id}-toolbar-container`"
      class="wangeditor-toolbar"
      :style="{ zIndex: zIndex + 1 }"
    ></div>
    <div
      :id="`${id}-editor-container`"
      :style="{
        height: height + 'px',
        zIndex: zIndex - 1,
        position: 'relative',
      }"
    >
      <monaco
        v-if="showHTML"
        autoFormat
        :id="`${id}-editor-monaco`"
        v-model="dataEdit"
        @on-change="changeHTML"
        :defaultTheme="dark ? 'vs-dark' : 'vs'"
        :showHeader="false"
        language="html"
        height="100%"
        :showFullscreen="false"
        ref="wangeditorMonaco"
        :style="{ zIndex: zIndex }"
        class="wang-editor-monaco"
      />
    </div>

    <materialCenter
      v-if="material"
      v-model="showMaterialImage"
      @on-change="selectImage"
      :maxSize="maxSize"
      multiple
    />

    <materialCenter
      v-if="material"
      mode="视频"
      v-model="showMaterialVideo"
      @on-change="selectVideo"
      :maxSize="maxSize"
    />

    <Dropdown
      trigger="custom"
      :visible="showMention"
      :style="{ position: 'absolute', top: top, left: left }"
    >
      <DropdownMenu slot="list">
        <div style="width: 200px">
          <Input
            v-model="searchKey"
            placeholder="输入用户名搜素"
            style="padding: 5px 8px 8px 8px"
            @on-change="searchUser"
            ref="searchInput"
          />
          <div
            class="ivu-dropdown-item"
            style="overflow: hidden"
            @click="insertUser(item)"
            v-for="(item, index) in userList"
            :key="index"
          >
            {{ item.nickname }}
            <span style="margin-left: 8px; color: #ccc">{{
              item.username
            }}</span>
          </div>
        </div>
      </DropdownMenu>
    </Dropdown>
  </div>
</template>

<script>
import { uploadFile, getUserListData } from "@/api/index";
import monaco from "@/views/my-components/bigdata/monaco";
import materialCenter from "@/views/my-components/bigdata/material-center";
import "@wangeditor/editor/dist/css/style.css";
import {
  createEditor,
  createToolbar,
  SlateEditor,
  Boot,
  DomEditor,
  SlateTransforms,
} from "@wangeditor/editor";
import { EditHTMLConf } from "./wangeditor/editHTML";
import { MaterialPicConf } from "./wangeditor/materialPic";
import { MaterialVideoConf } from "./wangeditor/materialVideo";
import attachmentModule from "@wangeditor/plugin-upload-attachment";
import mentionModule from "@wangeditor/plugin-mention";
Boot.registerModule(mentionModule);
Boot.registerMenu(EditHTMLConf);
Boot.registerMenu(MaterialPicConf);
Boot.registerMenu(MaterialVideoConf);
Boot.registerModule(attachmentModule);
export default {
  components: {
    monaco,
    materialCenter,
  },
  name: "editor",
  props: {
    id: {
      type: String,
      default: "editor",
    },
    value: String,
    border: {
      type: Boolean,
      default: true,
    },
    dark: {
      type: Boolean,
      default: false,
    },
    zIndex: {
      type: Number,
      default: 1,
    },
    height: {
      type: [Number, String],
      default: 360,
    },
    placeholder: {
      type: String,
      default: "请输入内容",
    },
    material: {
      type: Boolean,
      default: false,
    },
    maxSize: {
      type: Number,
      default: 5,
    },
    base64: {
      type: Boolean,
      default: false,
    },
    uploadPic: {
      type: Boolean,
      default: true,
    },
    uploadImgTimeout: {
      type: Number,
      default: 10000,
    },
    uploadVideo: {
      type: Boolean,
      default: true,
    },
    uploadVideoTimeout: {
      type: Number,
      default: 30000,
    },
    uploadAttachment: {
      type: Boolean,
      default: false,
    },
    uploadAttachTimeout: {
      type: Number,
      default: 20000,
    },
    expandHtml: {
      type: Boolean,
      default: true,
    },
    mention: {
      type: Boolean,
      default: false,
    },
    showFontFamily: {
      type: Boolean,
      default: false,
    },
    showLineHeight: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      editor: null,
      data: this.value, // 富文本数据
      dataEdit: "", // 编辑数据
      showHTML: false, // 显示html
      showMaterialImage: false,
      showMaterialVideo: false,
      showMention: false,
      searchKey: "",
      top: "0px",
      left: "0px",
      userLoading: false,
      userList: [],
    };
  },
  methods: {
    initEditor() {
      // 编辑器配置
      const editorConfig = {
        placeholder: this.placeholder,
        hoverbarKeys: {
          attachment: {
            menuKeys: ["downloadAttachment"], // “下载附件”菜单
          },
        },
        MENU_CONF: {
          uploadImage: {
            base64LimitSize: this.base64 ? this.maxSize * 1024 * 1024 : 0, // 5MB 0代表不启用
            server: uploadFile,
            fieldName: "file",
            maxFileSize: 0,
            allowedFileTypes: ["image/*"], // 选择文件时的类型限制，默认为 ['image/*'] 。如不想限制，则设置为 []
            // 自定义上传参数，例如传递验证的 token 等。参数会被添加到 formData 中，一起上传到服务端。
            meta: {
              accessToken: this.getStore("accessToken"),
            },
            timeout: this.uploadImgTimeout, // 超时时间，默认为 10 秒
            customInsert: (res, insertFn) => {
              if (res && res.success) {
                let url = res.result;
                insertFn(url);
                this.$Message.success("上传图片成功");
              } else {
                this.$Message.error(res.message);
              }
            },
            onError: (file, err, res) => {
              this.$Message.error(`${file.name} 上传出错，${err}`);
            },
          },
          uploadVideo: {
            server: uploadFile,
            fieldName: "file",
            maxFileSize: 0,
            allowedFileTypes: ["video/*"],
            meta: {
              accessToken: this.getStore("accessToken"),
            },
            timeout: this.uploadVideoTimeout,
            customInsert: (res, insertFn) => {
              if (res && res.success) {
                let url = res.result;
                insertFn(url);
                this.$Message.success("上传视频成功");
              } else {
                this.$Message.error(res.message);
              }
            },
            onError: (file, err, res) => {
              this.$Message.error(`${file.name} 上传出错，${err}`);
            },
          },
          uploadAttachment: {
            server: uploadFile,
            timeout: this.uploadAttachTimeout,
            fieldName: "file",
            meta: {
              accessToken: this.getStore("accessToken"),
            },
            maxFileSize: 0,
            customInsert: (res, file, insertFn) => {
              if (res && res.success) {
                let url = res.result;
                insertFn(`customInsert-${file.name}`, url);
                this.$Message.success("上传文件成功");
              } else {
                this.$Message.error(res.message);
              }
            },
            onError: (file, err, res) => {
              this.$Message.error(`${file.name} 上传出错，${err}`);
            },
          },
          editHTML: {
            onClick: () => {
              this.editHTML();
            },
          },
          materialPic: {
            onClick: () => {
              this.showMaterialImage = true;
            },
          },
          materialVideo: {
            onClick: () => {
              this.showMaterialVideo = true;
            },
          },
        },
        EXTEND_CONF: {
          mentionConfig: {
            showModal: (editor) => {
              if (!this.mention) {
                return;
              }
              // 获取光标位置，定位 modal
              const domSelection = document.getSelection();
              const domRange = domSelection.getRangeAt(0);
              if (domRange == null) {
                return;
              }
              const selectRect = domRange.getBoundingClientRect();
              this.top = selectRect.top - 90 + "px";
              this.left = selectRect.left - 140 + "px";

              this.searchKey = "";
              this.searchUser();
              this.showMention = true;
              setTimeout(() => {
                this.$refs.searchInput.focus({
                  cursor: "start",
                });
              }, 50);
            },
            hideModal: () => {
              this.showMention = false;
            },
          },
        },
      };

      // 创建编辑器
      const editor = createEditor({
        html: this.value || "",
        selector: `#${this.id}-editor-container`,
        mode: "default", // default 或 'simple'
        config: {
          ...editorConfig,
          onCreated: (editor) => {
            // 注意，一定要用 Object.seal() 否则会报错
            this.editor = Object.seal(editor);
            setTimeout(() => {
              const endPoint = SlateEditor.end(editor, []);
              editor.select(endPoint);
            }, 10);
          },
          onChange: (editor) => {
            // 当编辑器选区、内容变化时，即触发
            this.data = editor.getHtml();
            this.$emit("input", this.data);
            this.$emit("on-change", this.data);
          },
        },
      });
      editor.on("fullScreen", () => {
        if (this.showHTML) {
          this.$refs.wangeditorMonaco.layout();
        }
      });
      editor.on("unFullScreen", () => {
        if (this.showHTML) {
          this.$refs.wangeditorMonaco.layout();
        }
      });
      // 工具栏配置
      const toolbarConfig = {
        toolbarKeys: [
          "headerSelect",
          "fontSize",
          this.showFontFamily ? "fontFamily" : "|",
          this.showLineHeight ? "lineHeight" : "|",
          "|",
          "bold",
          "underline",
          "italic",
          "color",
          "bgColor",
          {
            key: "group-more-style",
            title: "更多",
            iconSvg:
              '<svg viewBox="0 0 1024 1024"><path d="M204.8 505.6m-76.8 0a76.8 76.8 0 1 0 153.6 0 76.8 76.8 0 1 0-153.6 0Z"></path><path d="M505.6 505.6m-76.8 0a76.8 76.8 0 1 0 153.6 0 76.8 76.8 0 1 0-153.6 0Z"></path><path d="M806.4 505.6m-76.8 0a76.8 76.8 0 1 0 153.6 0 76.8 76.8 0 1 0-153.6 0Z"></path></svg>',
            menuKeys: [
              "through",
              "code",
              "sup",
              "sub",
              "divider",
              "clearStyle",
            ],
          },
          "|",
          "bulletedList",
          "numberedList",
          "todo",
          "blockquote",
          {
            key: "group-justify",
            title: "对齐",
            iconSvg:
              '<svg viewBox="0 0 1024 1024"><path d="M768 793.6v102.4H51.2v-102.4h716.8z m204.8-230.4v102.4H51.2v-102.4h921.6z m-204.8-230.4v102.4H51.2v-102.4h716.8zM972.8 102.4v102.4H51.2V102.4h921.6z"></path></svg>',
            menuKeys: [
              "justifyLeft",
              "justifyRight",
              "justifyCenter",
              "justifyJustify",
              "indent",
              "delIndent",
            ],
          },
          "|",
          "emotion",
          "insertLink",
          this.material
            ? "materialPic"
            : {
                key: "group-image",
                title: "图片",
                iconSvg:
                  '<svg viewBox="0 0 1024 1024"><path d="M959.877 128l0.123 0.123v767.775l-0.123 0.122H64.102l-0.122-0.122V128.123l0.122-0.123h895.775zM960 64H64C28.795 64 0 92.795 0 128v768c0 35.205 28.795 64 64 64h896c35.205 0 64-28.795 64-64V128c0-35.205-28.795-64-64-64zM832 288.01c0 53.023-42.988 96.01-96.01 96.01s-96.01-42.987-96.01-96.01S682.967 192 735.99 192 832 234.988 832 288.01zM896 832H128V704l224.01-384 256 320h64l224.01-192z"></path></svg>',
                menuKeys: ["insertImage", "uploadImage"],
              },
          this.material
            ? "materialVideo"
            : {
                key: "group-video",
                title: "视频",
                iconSvg:
                  '<svg viewBox="0 0 1024 1024"><path d="M981.184 160.096C837.568 139.456 678.848 128 512 128S186.432 139.456 42.816 160.096C15.296 267.808 0 386.848 0 512s15.264 244.16 42.816 351.904C186.464 884.544 345.152 896 512 896s325.568-11.456 469.184-32.096C1008.704 756.192 1024 637.152 1024 512s-15.264-244.16-42.816-351.904zM384 704V320l320 192-320 192z"></path></svg>',
                menuKeys: ["insertVideo", "uploadVideo"],
              },
          "insertTable",
          "codeBlock",
          this.expandHtml ? "editHTML" : "|",
          "|",
          "undo",
          "redo",
          "|",
          "fullScreen",
        ],
      };
      if (this.uploadAttachment) {
        toolbarConfig.insertKeys = {
          index: 22,
          keys: ["uploadAttachment"],
        };
      }
      // 创建工具栏
      const toolbar = createToolbar({
        editor,
        selector: `#${this.id}-toolbar-container`,
        mode: "default", // default 或 'simple'
        config: toolbarConfig,
      });
    },
    insertUser(v) {
      const mentionNode = {
        type: "mention",
        value: v.nickname,
        info: {
          id: v.id,
          username: v.username,
        },
        children: [{ text: "" }],
      };

      this.editor.restoreSelection(); // 恢复选区
      this.editor.deleteBackward("character"); // 删除 '@'
      this.editor.insertNode(mentionNode); // 插入 mention
      this.editor.move(1); // 移动光标
    },
    searchUser() {
      let params = {
        id: "",
        nickname: this.searchKey,
        type: "",
        status: "",
        pageNumber: 1,
        pageSize: 5,
        sort: "nickname",
        order: "asc",
      };
      this.userLoading = true;
      getUserListData(params).then((res) => {
        this.userLoading = false;
        if (res.success) {
          this.userList = res.result.content;
        }
      });
    },
    insertImage(v) {
      // 新建一个 image node
      const image = {
        type: "image",
        src: v,
        href: "",
        alt: "",
        style: {},
        children: [{ text: "" }], // 【注意】void node 需要一个空 text 作为 children
      };
      // 如果 blur ，则恢复选区
      if (this.editor.selection == null) {
        this.editor.restoreSelection();
      }
      // 如果当前正好选中了图片，则 move 一下（如：连续上传多张图片时）
      if (DomEditor.getSelectedNodeByType(this.editor, "image")) {
        this.editor.move(1);
      }
      // 插入图片
      SlateTransforms.insertNodes(this.editor, image);
    },
    insertVideo(v) {
      const video = {
        type: "video",
        src: v,
        children: [{ text: "" }],
      };
      // 插入视频 不使用此方式会比正常的选区选取先执行
      Promise.resolve().then(() => {
        SlateTransforms.insertNodes(this.editor, video);
      });
    },
    selectImage(v) {
      v.forEach((e) => {
        this.insertImage(e);
      });
    },
    selectVideo(v) {
      this.insertVideo(v);
    },
    editHTML() {
      if (this.showHTML) {
        this.showHTML = false;
        this.editor.focus();
        return;
      }
      this.dataEdit = this.data;
      this.showHTML = true;
      setTimeout(() => {
        this.$refs.wangeditorMonaco.focus();
      }, 50);
    },
    changeHTML(v) {
      this.setHtml(v, true);
    },
    setHtml(newHtml, htmlMode) {
      if (this.editor == null) {
        return;
      }
      // 记录编辑器当前状态
      const isEditorDisabled = this.editor.isDisabled();
      const isEditorFocused = this.editor.isFocused();
      const editorSelectionStr = JSON.stringify(this.editor.selection);
      // 删除并重新设置 HTML
      this.editor.enable();
      if (!htmlMode) {
        this.editor.focus();
      }
      this.editor.select([]);
      this.editor.deleteFragment();
      // @ts-ignore
      SlateTransforms.setNodes(
        this.editor,
        { type: "paragraph" },
        { mode: "highest" }
      );
      this.editor.dangerouslyInsertHtml(newHtml);
      // 恢复编辑器状态
      if (!htmlMode) {
        if (!isEditorFocused) {
          this.editor.deselect();
          this.editor.blur();
          return;
        }
      }
      if (isEditorDisabled) {
        this.editor.deselect();
        this.editor.disable();
        return;
      }
      try {
        this.editor.select(JSON.parse(editorSelectionStr)); // 选中原来的位置
      } catch (ex) {
        this.editor.select(SlateEditor.start(this.editor, [])); // 选中开始
      }
    },
    setData(value) {
      if (!this.editor) {
        return;
      }
      if (value != this.data) {
        this.data = value;
        this.setHtml(this.data);
        this.$emit("input", this.data);
        this.$emit("on-change", this.data);
      }
    },
  },
  beforeDestroy() {
    // 调用销毁 API 对当前编辑器实例进行销毁
    if (this.editor != null) {
      this.editor.destroy();
    }
  },
  watch: {
    value(val) {
      this.setData(val);
    },
  },
  mounted() {
    if (this.editor == null) {
      this.initEditor();
    }
  },
};
</script>

<style lang="less">
.w-e-toolbar p,
.w-e-text-container p,
.w-e-menu-panel p {
  font-size: 1em !important;
}

.wang-editor-monaco {
  position: absolute !important;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.w-e-full-screen-container {
  z-index: 10002;
}

.wang-editor {
  border: 1px solid #c9d8db;
  .wangeditor-toolbar {
    border-bottom: 1px solid #eee;
  }
}

.wang-editor-dark {
  border: none;
  .wangeditor-toolbar {
    border-bottom: none;
  }
  --w-e-textarea-bg-color: #1e1e1e;
  --w-e-textarea-color: #d4d4d4;
  --w-e-textarea-border-color: #4a4a4a;
  --w-e-textarea-slight-border-color: #4a4a4a;
  --w-e-textarea-slight-color: #d4d4d4;
  --w-e-textarea-slight-bg-color: #383838;
  --w-e-textarea-selected-border-color: #007aff; // 选中的元素，如选中了分割线
  --w-e-textarea-handler-bg-color: #4290f7; // 工具，如图片拖拽按钮

  --w-e-toolbar-color: #8a8a8a;
  --w-e-toolbar-bg-color: #282a2e;
  --w-e-toolbar-active-color: #afb4bd;
  --w-e-toolbar-active-bg-color: #383838;
  --w-e-toolbar-disabled-color: #5d5d5d;
  --w-e-toolbar-border-color: #4a4a4a;

  --w-e-modal-button-bg-color: #282a2e;
  --w-e-modal-button-border-color: #4a4a4a;
}
</style>