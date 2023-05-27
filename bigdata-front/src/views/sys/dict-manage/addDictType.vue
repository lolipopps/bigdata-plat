<template>
  <div>
    <Modal
      title="添加字典"
      v-model="visible"
      :mask-closable="false"
      :width="500"
    >
      <Form
        ref="form"
        :model="form"
        :label-width="85"
        :rules="formValidate"
      >
        <FormItem label="字典名称" prop="title">
          <Input v-model="form.title" />
        </FormItem>
        <FormItem label="字典类型" prop="type" class="block-tool">
          <Tooltip
            placement="right"
            :max-width="220"
            transfer
            content="建议设置为英文名且需唯一 非开发人员谨慎修改"
          >
            <Input v-model="form.type" />
          </Tooltip>
        </FormItem>
        <FormItem label="备注" prop="description">
          <Input v-model="form.description" />
        </FormItem>
        <FormItem label="排序值" prop="sortOrder">
          <Tooltip
            trigger="hover"
            placement="right"
            content="值越小越靠前，支持小数"
          >
            <InputNumber
              :max="1000"
              :min="0"
              v-model="form.sortOrder"
            ></InputNumber>
          </Tooltip>
        </FormItem>
      </Form>
      <div slot="footer">
        <Button type="text" @click="visible = false">取消</Button>
        <Button
          type="primary"
          :loading="submitLoading"
          @click="submit"
          >提交</Button
        >
      </div>
    </Modal>
  </div>
</template>

<script>
import { addDict } from "@/api/index";
export default {
  name: "addDictType",
  components: {},
  props: {
    value: {
      type: Boolean,
      default: false,
    },
    dataLength: {
      type: Number
    },
  },
  data() {
    return {
      visible: this.value,
      submitLoading: false,
      form: {
        title: "",
        type: "",
        description: "",
        sortOrder: 0,
      },
      formValidate: {
        // 表单验证规则
        title: [{ required: true, message: "请输入", trigger: "blur" }],
        type: [{ required: true, message: "请输入", trigger: "blur" }],
        sortOrder: [
          {
            required: true,
            type: "number",
            message: "请输入排序值",
            trigger: "blur",
          },
        ],
      },
    };
  },
  methods: {
    init() {},
    submit() {
      this.$refs.form.validate((valid) => {
        if (valid) {
          // 添加
          this.submitLoading = true;
          addDict(this.form).then((res) => {
            this.submitLoading = false;
            if (res.success) {
              this.$Message.success("操作成功");
              this.$emit("on-submit", true);
              this.visible = false;
            }
          });
        }
      });
    },
    setCurrentValue(value) {
      if (value === this.visible) {
        return;
      }
      // 清空数据
      this.$refs.form.resetFields();
      this.form.sortOrder = this.dataLength;
      this.visible = value;
    },
  },
  watch: {
    value(val) {
      this.setCurrentValue(val);
    },
    visible(value) {
      this.$emit("input", value);
    },
  },
  mounted() {
    this.init();
  },
};
</script>

<style lang="less">
@import "@/styles/drawer-common.less";
</style>

