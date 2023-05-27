const theme = {
    state: {
        theme: {
            mainTheme: "", // primary light topNav darkmode
            menuTheme: "darkblue", // 侧边菜单主题 darkblue light dark black
            navTheme: "light", // 导航栏主题 light darkblue primary black
            accordion: true, // 侧边菜单手风琴模式
            menuWidth: 220, // 侧边菜单宽度
            navType: "1", // 导航菜单展示方式 1菜单模式 2单个下拉菜单 3图标
            sliceNum: 4, // 导航菜单顶部菜单模式时默认显示个数
            navShowType: "app", // 下拉菜单展示方式 app图标 list列表（navType 1、3）
            showNavMenuIcon: true, // 是否显示图标（navType 1）
            fixNav: false, // 是否固定顶部导航
            showFold: true, // 显示折叠菜单图标
            showLogo: true, // 是否显示Logo
            showTags: true, // 是否显示多标签
            showSearch: true, // 是否显示搜索菜单
            showFooter: true, // 是否显示页脚
            watermarkConfig: {
                open: true, // 开启水印
                type: 0, // 水印内容类型 默认0当前登录用户账号 1当前登录用户手机号 2自定义
                custom_txt: "", // 水印内容类型为自定义时显示的内容
                watermark_alpha: 0.05, // 水印不透明度
                watermark_fontsize: 12, // 水印字体大小
                watermark_angle: 15, // 水印倾斜度数
                watermark_x: 60, // 水印起始位置x轴坐标
                watermark_y: 100, // 水印起始位置Y轴坐标
                watermark_x_space: 100, // 水印x轴间隔
                watermark_y_space: 50, // 水印y轴间隔
                watermark_rows: 0, // 水印行数
                watermark_cols: 0, // 水印列数
                watermark_font: '微软雅黑', // 水印字体
                watermark_color: 'black', // 水印字体颜色
                watermark_width: 100, // 水印宽度
                watermark_height: 100, // 水印长度
                watermark_parent_width: 0, // 水印的总体宽度
                watermark_parent_height: 0 // 水印的总体高度
            }, // 水印配置
            weakMode: false, // 色弱模式
        }
    },
    mutations: {
        // Theme赋值
        setTheme(state, v) {
            if (v && v != "undefined") {
                let data = JSON.parse(v);
                if (!data.watermarkConfig) {
                    data.watermarkConfig = state.theme.watermarkConfig;
                }
                state.theme = data;
                localStorage.theme = JSON.stringify(state.theme);
            }
        },
        setMainTheme(state, v) {
            state.theme.mainTheme = v;
            localStorage.theme = JSON.stringify(state.theme);
        },
        setMenuTheme(state, v) {
            state.theme.menuTheme = v;
            localStorage.theme = JSON.stringify(state.theme);
        },
        setNavTheme(state, v) {
            state.theme.navTheme = v;
            localStorage.theme = JSON.stringify(state.theme);
        },
        setAccordion(state, v) {
            state.theme.accordion = v;
            localStorage.theme = JSON.stringify(state.theme);
        },
        setMenuWidth(state, v) {
            state.theme.menuWidth = v;
            localStorage.theme = JSON.stringify(state.theme);
        },
        setNavType(state, v) {
            state.theme.navType = v;
            localStorage.theme = JSON.stringify(state.theme);
        },
        setSliceNum(state, v) {
            state.theme.sliceNum = v;
            localStorage.theme = JSON.stringify(state.theme);
        },
        setNavShowType(state, v) {
            state.theme.navShowType = v;
            localStorage.theme = JSON.stringify(state.theme);
        },
        setShowNavMenuIcon(state, v) {
            state.theme.showNavMenuIcon = v;
            localStorage.theme = JSON.stringify(state.theme);
        },
        setFixNav(state, v) {
            state.theme.fixNav = v;
            localStorage.theme = JSON.stringify(state.theme);
        },
        setShowFold(state, v) {
            state.theme.showFold = v;
            localStorage.theme = JSON.stringify(state.theme);
        },
        setShowLogo(state, v) {
            state.theme.showLogo = v;
            localStorage.theme = JSON.stringify(state.theme);
        },
        setShowTags(state, v) {
            state.theme.showTags = v;
            localStorage.theme = JSON.stringify(state.theme);
        },
        setShowSearch(state, v) {
            state.theme.showSearch = v;
            localStorage.theme = JSON.stringify(state.theme);
        },
        setShowFooter(state, v) {
            state.theme.showFooter = v;
            localStorage.theme = JSON.stringify(state.theme);
        },
        setWatermark(state, v) {
            state.theme.watermarkConfig.open = v;
            localStorage.theme = JSON.stringify(state.theme);
        },
        setWMAlpha(state, v) {
            state.theme.watermarkConfig.watermark_alpha = v;
            localStorage.theme = JSON.stringify(state.theme);
        },
        setWMAFontsize(state, v) {
            state.theme.watermarkConfig.watermark_fontsize = v;
            localStorage.theme = JSON.stringify(state.theme);
        },
        setWeakMode(state, v) {
            state.theme.weakMode = v;
            localStorage.theme = JSON.stringify(state.theme);
        }
    }
};

export default theme;
