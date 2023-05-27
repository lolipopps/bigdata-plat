import Main from '@/views/Main.vue';

// 不作为Main组件的子页面展示的页面单独写，如下
export const loginRouter = {
    path: '/login',
    name: 'login',
    meta: {
        title: '登录 - 大数据平台 '
    },
    component: () => import('@/views/login.vue')
};

export const loginQRRouter = {
    path: '/login-qr',
    name: 'login-qr',
    meta: {
        title: '扫码登录 - 大数据平台 '
    },
    component: () => import('@/views/login-qr.vue')
};

export const registRouter = {
    path: '/register',
    name: 'register',
    meta: {
        title: '注册 - 大数据平台'
    },
    component: () => import('@/views/register.vue')
};

export const registResult = {
    path: '/register-result',
    name: 'register-result',
    meta: {
        title: '注册结果 - 大数据平台'
    },
    component: () => import('@/views/register-result.vue')
};

export const reset = {
    path: '/reset',
    name: 'reset',
    meta: {
        title: '重置密码 - 大数据平台'
    },
    component: () => import('@/views/reset.vue')
};

export const relateRouter = {
    path: '/relate',
    name: 'relate',
    meta: {
        title: '绑定账号 - 大数据平台 '
    },
    component: () => import('@/views/relate.vue')
};

export const authorizeRouter = {
    path: '/authorize',
    name: 'authorize',
    meta: {
        title: 'bigdata统一认证平台 - 大数据平台 '
    },
    component: () => import('@/views/authorize.vue')
};

// export const page404 = {
//     path: '/*',
//     name: 'error-404',
//     meta: {
//         title: '404-页面不存在'
//     },
//     component: () => import('@/views/error-page/404.vue')
// };

export const page403 = {
    path: '/403',
    meta: {
        title: '403-权限不足'
    },
    name: 'error-403',
    component: () => import('@/views/error-page/403.vue')
};

export const page500 = {
    path: '/500',
    meta: {
        title: '500-服务端错误'
    },
    name: 'error-500',
    component: () => import('@/views/error-page/500.vue')
};

// 作为Main组件的子页面展示但是不在左侧菜单显示的路由写在otherRouter里
export const otherRouter = {
    path: '/',
    name: 'otherRouter',
    redirect: '/home',
    component: Main,
    children: [
        { path: 'home', title: '首页', localize: true, i18n: 'home', name: 'home_index', component: () => import('@/views/home/home.vue') },
        { path: 'ownspace', title: '个人中心', name: 'ownspace_index', component: () => import('@/views/own-space/own-space.vue') },
        { path: 'message', title: '消息中心', name: 'message_index', component: () => import('@/views/message/message.vue') },
        { path: 'add', title: '添加', name: 'add', component: () => import('@/views/bigdata-vue-template/new-window/add.vue') },
        { path: 'edit', title: '编辑', name: 'edit', component: () => import('@/views/bigdata-vue-template/new-window/edit.vue') },
        { path: 'leave', title: '请假申请', name: 'leave', component: () => import('@/views/activity/business/leave.vue') },
        { path: 'historic-detail', title: '流程进度历史详情', name: 'historic_detail', component: () => import('@/views/activity/historic-detail/historicDetail.vue') }
    ]
};

export const appRouter = [];

// 所有上面定义的路由都要写在下面的routers里
export const routers = [
    loginRouter,
    loginQRRouter,
    registRouter,
    registResult,
    reset,
    relateRouter,
    authorizeRouter,
    otherRouter,
    ...appRouter,
    page500,
    page403
];
