<script setup lang="ts">
import { useRouter } from 'vue-router'
import {checkAuthStatus, logout} from "@/api";
import {defineEmits, onMounted, ref} from "vue";

const emit = defineEmits(['logout'])

let router = useRouter()

const userName = ref<String>("");

onMounted(async () => {
    let res = await checkAuthStatus()
    userName.value = res.user;
})

async function handleLogout() {
  emit('logout')
  let res = await logout();
  if(res.ok) {
    await router.push("/login")
  }
}

</script>

<template>
  <i class="pi pi-user" style="font-size: 2.5rem"></i>
  <label>Hello, {{ userName }}!</label>
  <div>
    <button>
      <i class="pi pi-question" style="font-size: 0.8rem"></i>
      Help
    </button>
    <button @click="handleLogout">
      <i class="pi pi-sign-out" style="font-size: 0.8rem"></i>
      Logout
    </button>
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