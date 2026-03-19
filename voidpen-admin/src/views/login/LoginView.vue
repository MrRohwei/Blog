<template>
  <div class="login-wrap">
    <div class="login-card">
      <div class="login-head">
        <h1>Voidpen</h1>
        <p>虚笔后台管理系统</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" size="large" clearable />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-button type="primary" size="large" :loading="loading" class="login-btn" @click="handleLogin">
          登录
        </el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function handleLogin() {
  if (!formRef.value) {
    return
  }

  await formRef.value.validate()
  loading.value = true
  try {
    await userStore.loginAction(form.username.trim(), form.password)
    ElMessage.success('登录成功')
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/dashboard'
    router.replace(redirect)
  } catch (error) {
    ElMessage.error(error?.message || '登录失败，请检查用户名和密码')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-wrap {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 24px;
  background: radial-gradient(circle at top, #dbeafe 0%, #eff6ff 35%, #f8fafc 100%);
}

.login-card {
  width: 100%;
  max-width: 420px;
  padding: 32px;
  border-radius: 16px;
  background: #ffffff;
  border: 1px solid #dbeafe;
  box-shadow: 0 20px 45px rgba(15, 23, 42, 0.08);
}

.login-head {
  text-align: center;
  margin-bottom: 24px;
}

.login-head h1 {
  margin: 0;
  font-size: 30px;
  color: #0f172a;
}

.login-head p {
  margin: 8px 0 0;
  color: #64748b;
}

.login-btn {
  width: 100%;
  margin-top: 8px;
}
</style>
