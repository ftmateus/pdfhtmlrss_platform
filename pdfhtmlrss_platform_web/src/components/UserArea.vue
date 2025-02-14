<script setup lang="ts">
import { useRouter } from 'vue-router'
import {checkAuthStatus, logout} from "@/api";
import {onMounted, ref} from "vue";

let router = useRouter()

const userName = ref<String>("");

onMounted(async () => {
    let res = await checkAuthStatus()
    userName.value = res.user;
})

async function handleLogout() {
  let res = await logout();
  if(res.ok) {
    await router.push("/login")
  }
}

</script>

<template>
  <label>Hello, {{ userName }}!</label>
  <div>
    <button>Help</button>
    <button @click="handleLogout">Logout</button>
  </div>
</template>

<style scoped>
div {
  display: flex;
  flex-direction: row;
  width: 175px;
  justify-content: center;
  gap: 10px;
  align-content: center;
}
</style>