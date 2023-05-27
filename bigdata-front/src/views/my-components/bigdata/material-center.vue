<template>
  <div>
    <!-- 820 -->
    <Modal :title="`我的${mode}`" v-model="modalVisible" :width="904">
      <div v-show="!urlMode">
        <div class="upload-nav">
          <div style="display: flex">
            <Upload :action="uploadFileUrl" :headers="accessToken" :data="uploadData" :on-success="handleSuccess" :on-error="handleError" :format="format" :accept="accept" :max-size="maxSize * 1024" :on-format-error="handleFormatError"
                    :on-exceeded-size="handleMaxSize" :before-upload="beforeUpload" :show-upload-list="false" v-if="showUpload">
              <Button :loading="loading" :icon="icon" :disabled="!searchForm.categoryId">上传{{ mode }}</Button>
            </Upload>
            <Button @click="urlMode = true" icon="ios-image-outline" style="margin-left: 5px" :disabled="!searchForm.categoryId" v-if="showInput">网络{{ mode }}</Button>
            <Input v-model="searchForm.title" suffix="ios-search" @on-change="getDataList" placeholder="输入文件名搜索" clearable style="width: 230px; margin-left: 5px" />
          </div>
          <Button @click="getDataList" icon="md-refresh">刷新</Button>
        </div>
        <div class="content-wrap">

          <!-- 内容 -->
          <div class="content">
            <div v-show="total == 0" class="empty">
              暂无数据，点击左上角上传按钮添加
            </div>
            <div v-show="total > 0" class="wrap">
              <div :class="{ item: !item.selected, 'item active': item.selected }" v-for="(item, index) in data" :key="index" :style="{
                  marginRight: (index + 1) % 5 == 0 ? '0px' : '10px',
                  marginBottom: index < 5 ? '20px' : '0px',
                }">
                <div class="preview" @click="changeSelect(item)">
                  <div class="collect-icon" v-if="item.isCollect">
                    <Icon type="md-star" color="#ffc60a" size="16" />
                  </div>
                  <img :src="item.url" class="img" v-if="mode == '图片'" />
                  <div v-else class="video">
                    <video class="cover">
                      <source :src="item.url + '#t=1'" preload="metadata" />
                    </video>
                    <img class="play" src="@/assets/icon/play.png" />
                  </div>
                  <div class="border" v-show="item.selected"></div>
                  <div class="border-hover"></div>
                  <img src="@/assets/icon/select.png" class="select" v-show="item.selected" />
                </div>
                <Tooltip :content="`${item.title} ${util.getFileSize(item.size)}`" placement="top" :max-width="500">
                  <div class="title">{{ item.title }}</div>
                </Tooltip>
                <div class="ops" v-if="showMenus">
                  <a @click="preview(item)">预览</a>
                  <Divider type="vertical" />
                  <a v-clipboard:copy="item.url" v-clipboard:success="onCopy" v-clipboard:error="onError" v-if="readonly">
                    复制链接
                  </a>
                  <Poptip width="350" v-if="!readonly"><a>编辑</a>
                    <div class="edit" slot="content">
                      文件名：
                      <Input v-model="item.newName" style="width: 280px" search enter-button="提交" @on-search="editName(item)"></Input>
                    </div>
                  </Poptip>
                  <Divider type="vertical" v-if="!readonly" />
                  <Dropdown v-if="!readonly">
                    <a>更多</a>
                    <DropdownMenu slot="list" class="block-pop">
                      <div class="ivu-dropdown-item" v-clipboard:copy="item.url" v-clipboard:success="onCopy" v-clipboard:error="onError">
                        复制链接
                      </div>
                      <div class="ivu-dropdown-item" @click="collect(item)">
                        {{ item.isCollect ? "取消收藏" : "收藏" }}
                      </div>
                      <Poptip confirm title="确认将此素材移动至回收站？" @on-ok="del(item)">
                        <div class="ivu-dropdown-item">删除</div>
                      </Poptip>
                    </DropdownMenu>
                  </Dropdown>
                </div>
              </div>
              <Spin v-show="dataLoading" fix></Spin>
            </div>
            <div class="bottom">
              <div class="select-count">
                {{
                  selectList.length > 0 ? `已选 ${selectList.length} 项` : ""
                }}
              </div>
              <Page :current="searchForm.pageNumber" :total="total" :page-size="searchForm.pageSize" @on-change="changePage" size="small" show-total show-elevator></Page>
            </div>
          </div>
        </div>
      </div>
      <div v-show="urlMode">
        <div class="upload-nav">
          <Button @click="urlMode = false" icon="ios-arrow-back">返回</Button>
        </div>
        <div style="width: 100%; display: flex">
          <Form ref="form" :model="form" label-position="left" :label-width="100" :rules="ruleValidate" style="margin-right: 10px">
            <FormItem :label="`${mode}链接`" prop="url">
              <Input v-model="form.url" type="textarea" :rows="5" :maxlength="maxlength" show-word-limit :placeholder="`请输入${mode}链接...`" :style="`width: ${mode == '图片' ? '600px' : '720px'}`" />
            </FormItem>
          </Form>
          <div v-if="mode == '图片'">
            <Avatar v-if="!form.url" shape="square" icon="md-images" size="110" />
            <Avatar v-else shape="square" :src="form.url" size="110" />
          </div>
        </div>
      </div>
      <div slot="footer">
        <Button type="text" @click="modalVisible = false">取消</Button>
        <Button :disabled="
            (selectList.length == 0 && !urlMode) || (!form.url && urlMode)
          " type="primary" @click="handelSubmit">确定</Button>
      </div>
    </Modal>
    <Modal v-model="videoVisible" :title="videoTitle" :width="800" @on-cancel="closeVideo" draggable footer-hide v-if="mode == '视频'">
      <div :id="videoId" style="height: 500px"></div>
    </Modal>
  </div>
</template>

<script>
import "viewerjs/dist/viewer.css";
import Viewer from "viewerjs";
import DPlayer from "dplayer";
import util from "@/libs/util";
import {
  uploadFile,
  getUserFileListData,
  renameUserFile,
  trashFile,
  collectFile,
} from "@/api/index";

export default {
  name: "materialCenter",

  props: {
    value: {
      type: Boolean,
      default: false,
    },
    mode: {
      validator(value) {
        return util.oneOf(value, ["图片", "视频"]);
      },
      type: String,
      default: "图片",
    },
    multiple: {
      type: Boolean,
      default: false,
    },
    readonly: {
      type: Boolean,
      default: false,
    },
    showMenus: {
      type: Boolean,
      default: true,
    },
    showUpload: {
      type: Boolean,
      default: true,
    },
    showInput: {
      type: Boolean,
      default: true,
    },
    maxlength: { type: Number, default: 250 },
    icon: {
      type: String,
      default: "ios-cloud-upload-outline",
    },
    maxSize: {
      type: Number,
      default: 5,
    },
    acceptImg: {
      type: String,
      default: ".jpg, .jpeg, .png, .gif",
    },
    acceptVideo: {
      type: String,
      default: ".mp4, .wmv, .avi, .mov, .flv, .mkv",
    },
  },
  computed: {
    format() {
      if (this.accept) {
        let format = [];
        this.accept.split(",").forEach((e) => {
          format.push(e.replace(".", "").replace(" ", ""));
        });
        return format;
      } else {
        return [];
      }
    },
  },
  data() {
    return {
      uploadData: {},
      accessToken: {},
      accept: "",
      urlMode: false,
      modalVisible: this.value,
      loading: false,
      dataLoading: false,
      uploadFileUrl: uploadFile,
      searchForm: {
        title: "",
        type: "image",
        pageNumber: 1,
        pageSize: 10,
        categoryId: "0",
        isCollect: null,
      },
      total: 0,
      data: [],
      selectList: [],
      form: {
        url: "",
      },
      ruleValidate: {
        url: [
          {
            required: true,
            message: "请输入合法的URL链接",
            type: "url",
          },
        ],
      },
      dp: null,
      videoVisible: false,
      videoTitle: "",
      videoId: "dplayer",
    };
  },
  methods: {
    init() {
      this.accessToken = {
        accessToken: this.getStore("accessToken"),
      };
      this.changeAccept();
      this.getDataList();
    },
    selectCategory(v) {
      if (v.id == "-1") {
        // 收藏夹
        this.searchForm.categoryId = "";
        this.searchForm.isCollect = true;
        this.getDataList();
      } else {
        this.searchForm.isCollect = null;
        this.searchForm.categoryId = v.id;
        this.uploadData = {
          categoryId: v.id,
        };
        this.currCate = v.title;
        this.getDataList();
      }
    },
    changeAccept() {
      if (this.mode == "图片") {
        this.accept = this.acceptImg;
      } else if (this.mode == "视频") {
        this.accept = this.acceptVideo;
      }
    },
    changePage(v) {
      this.searchForm.pageNumber = v;
      this.getDataList();
    },
    getDataList() {
      this.dataLoading = true;
      if (this.mode == "视频") {
        this.searchForm.type = "video";
      }
      getUserFileListData(this.searchForm).then((res) => {
        this.dataLoading = false;
        if (res.success) {
          this.data = res.result.content;
          this.data.forEach((e) => {
            e.newName = e.title;
            e.selected = false;
            e.border = false;
          });
          this.total = res.result.totalElements;
          if (this.data.length == 0 && this.searchForm.pageNumber > 1) {
            this.searchForm.pageNumber -= 1;
            this.getDataList();
          }
        }
      });
    },
    clearSelectAll() {
      this.selectList = [];
    },
    changeSelect(v) {
      v.selected = !v.selected;
      if (this.multiple) {
        if (v.selected) {
          this.selectList.push(v);
        } else {
          this.selectList = this.selectList.filter((e) => {
            return e.id != v.id;
          });
        }
      } else {
        if (v.selected) {
          if (this.selectList.length > 0) {
            this.selectList[0].selected = false;
            this.selectList = [];
          }
          this.selectList.push(v);
        } else {
          this.selectList = [];
        }
      }
    },
    handelSubmit() {
      let url = [];
      if (!this.urlMode) {
        this.selectList.forEach((e) => {
          url.push(e.url);
        });
        if (this.multiple) {
          this.$emit("on-change", url);
        } else {
          this.$emit("on-change", url[0]);
        }
        this.modalVisible = false;
      } else {
        this.$refs.form.validate((valid) => {
          if (valid) {
            url.push(this.form.url);
            if (this.multiple) {
              this.$emit("on-change", url);
            } else {
              this.$emit("on-change", url[0]);
            }
            this.modalVisible = false;
          }
        });
      }
    },
    preview(v) {
      if (this.mode == "图片") {
        this.viewImage(v);
      } else {
        this.showVideo(v);
      }
    },
    viewImage(v) {
      let image = new Image();
      image.src = v.url;
      let viewer = new Viewer(image, {
        hidden: function () {
          viewer.destroy();
        },
      });
      viewer.show();
    },
    showVideo(v) {
      this.dp = new DPlayer({
        container: document.getElementById(this.videoId),
        screenshot: true,
        video: {
          url: v.url,
        },
      });
      this.videoTitle = v.title;
      this.videoVisible = true;
    },
    closeVideo() {
      if (this.dp) {
        this.dp.destroy();
      }
    },
    collect(v) {
      collectFile({ id: v.id }).then((res) => {
        this.$Modal.remove();
        if (res.success) {
          this.$Message.success(v.isCollect ? "取消收藏成功" : "收藏成功");
          this.getDataList();
        }
      });
    },
    onCopy() {
      this.$Message.success("复制链接成功");
    },
    onError() {
      this.$Message.warning("复制链接失败，请手动复制");
    },
    editName(v) {
      renameUserFile(v).then((res) => {
        if (res.success) {
          this.$Message.success("操作成功");
          this.getDataList();
        }
      });
    },
    del(v) {
      trashFile({ ids: v.id }).then((res) => {
        if (res.success) {
          this.$Message.success("删除成功");
          this.getDataList();
        }
      });
    },
    handleFormatError(file) {
      this.loading = false;
      this.$Notice.warning({
        title: "不支持的文件格式",
        desc:
          "所选文件‘ " +
          file.name +
          " ’格式不正确, 请选择 " +
          this.accept +
          " 格式文件",
      });
    },
    handleMaxSize(file) {
      this.loading = false;
      this.$Notice.warning({
        title: "文件大小过大",
        desc:
          "所选文件‘ " +
          file.name +
          " ’大小过大, 不得超过 " +
          this.maxSize +
          "M.",
      });
    },
    beforeUpload() {
      this.loading = true;
      return true;
    },
    handleSuccess(res, file) {
      this.loading = false;
      if (res.success) {
        this.$Message.success("上传成功");
        this.getDataList();
      } else {
        this.$Message.error(res.message);
      }
    },
    handleError(error, file, fileList) {
      this.loading = false;
      this.$Message.error(error.toString());
    },
    setCurrentValue(value) {
      if (value === this.modalVisible) {
        return;
      }
      this.modalVisible = value;
    },
  },
  watch: {
    mode(val) {
      this.changeAccept();
    },
    value(val) {
      this.setCurrentValue(val);
    },
    modalVisible(value) {
      this.$emit("input", value);
    },
  },
  mounted() {
    this.init();
  },
};
</script>
<style lang="less" scoped>
.upload-nav {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}
.content-wrap {
  display: flex;

  .category {
    width: 216px;
    padding-right: 16px;
    border-right: 1px solid #dcdee0;
  }

  .content {
    height: 400px;
    width: 640px;
    margin-left: 16px;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    .empty {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      height: 90%;
      color: #999;
    }
    .wrap {
      position: relative;
      display: flex;
      flex-wrap: wrap;
      .item {
        width: 120px;
        .preview {
          position: relative;
          height: 120px;
          width: 120px;
          .collect-icon {
            position: absolute;
            right: 5px;
            top: 5px;
            background: #fff;
            z-index: 1;
            border-radius: 50%;
            width: 18px;
            height: 18px;
            display: flex;
            align-items: center;
            justify-content: center;
          }
          .img {
            height: 100%;
            width: 100%;
            object-fit: cover;
            cursor: pointer;
            background: #f3f3f3;
            transition: all 0.2s;
          }
          .video {
            height: 100%;
            width: 100%;
            cursor: pointer;
            position: relative;
            background: #f3f3f3;
            transition: all 0.2s;
            .cover {
              height: 100%;
              width: 100%;
              object-fit: fill;
            }
            .play {
              position: absolute;
              top: 41px;
              left: 41px;
              height: 40px;
              width: 40px;
              opacity: 0.8;
            }
          }
          .select {
            position: absolute;
            right: 1px;
            bottom: 2px;
            width: 30px;
            height: 30px;
          }
          .border {
            position: absolute;
            top: 0;
            left: 0;
            height: 100%;
            width: 100%;
            border: 2px solid #2d8cf0;
            overflow: hidden;
            pointer-events: none;
          }
          .border-hover {
            position: absolute;
            display: none;
            top: 0;
            left: 0;
            height: 100%;
            width: 100%;
            border: 2px solid #2d8cf0;
            overflow: hidden;
            pointer-events: none;
          }
          &:hover {
            .border-hover {
              display: block !important;
            }
          }
        }

        .title {
          margin-top: 5px;
          width: 120px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          cursor: pointer;
        }
        .ops {
          display: flex;
          align-items: center;
          .edit {
            padding: 5px 0;
            display: flex;
            align-items: center;
          }
        }
      }
      .active {
        .img {
          transition: all 0.2s;
          border-color: #2d8cf0;
        }
      }
    }
  }
}
.bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  .select-count {
    color: #999;
  }
}
</style>

