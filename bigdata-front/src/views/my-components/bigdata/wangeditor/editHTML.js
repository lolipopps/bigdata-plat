
class EditHTML {
  constructor() {
    this.title = "编辑HTML";
    this.iconSvg =
      '<svg t="1650724174781" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="10271" width="200" height="200"><path d="M38 0.547h948.087l-77.551 900.025L506.584 1024 124.292 900.572 38 0.547zM797.126 272.52l22.937-136.53-610.578-1.095 39.322 409.6h397.585v107.041l-133.255 46.968-138.72-49.15-8.734-57.892-109.23 1.094 16.386 144.177 233.745 81.923 245.76-77.551 31.672-329.866H351.48l-10.921-138.718h456.568z m0 0" p-id="10272"></path></svg>';
    this.tag = "button";
    this.alwaysEnable = true;
    this.isHtmlOpen = false;
  }
  getValue(editor) {
    return "";
  }
  isActive(editor) {
    return this.isHtmlOpen;
  }
  isDisabled(editor) {
    return false;
  }
  exec(editor, value) {
    this.isHtmlOpen = !this.isHtmlOpen;
    this.isActive(editor)
    const { onClick } = editor.getMenuConfig('editHTML');
    onClick(() => {

    });
  }
}
export const EditHTMLConf = {
  key: "editHTML",
  factory() {
    return new EditHTML();
  },
};

