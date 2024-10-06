<template>
  <!-- 
  This component is used to render the menu items and sub-menu items recursively. The base case is
  when the menuItem.children is undefined, then it renders an el-menu-item. Otherwise, it renders an
  el-sub-menu and calls itself recursively to render the children. 
  -->
  <template v-for="menuItem in menuRoute.children" :key="menuItem.path">
    <!-- base case: this menu item has no children -->
    <el-menu-item
      :index="menuItem.path"
      v-if="
        menuItem.children === undefined &&
        hasPermission(menuItem.meta.requiresPermissions) &&
        menuItem.meta.isMenuItem
      "
    >
      <!--Put icon outside the slot, otherwise, when collapsing menu, it renders strangely.-->
      <el-icon>
        <component :is="menuItem.meta.icon" />
      </el-icon>
      <template #title>
        <span>{{ menuItem.meta.title }}</span>
      </template>
    </el-menu-item>
    <!-- recursive case -->
    <el-sub-menu
      :index="menuItem.path"
      v-if="
        menuItem.children &&
        hasPermission(menuItem.meta.requiresPermissions) &&
        menuItem.meta.isMenuItem
      "
    >
      <template #title>
        <el-icon>
          <component :is="menuItem.meta.icon" />
        </el-icon>
        <span>{{ menuItem.meta.title }}</span>
      </template>
      <!-- recursive call: recursively render its children menu items -->
      <MenuItems :menuRoute="menuItem"></MenuItems>
    </el-sub-menu>
  </template>
</template>

<script setup lang="ts">
defineProps(['menuRoute']) // menuRoute is the route object that contains the children
import { useUserInfoStore } from '@/stores/userInfo'

const userInfoStore = useUserInfoStore()
let userPermissions: string[] = []

if (userInfoStore.userInfo) {
  // if userInfo is not null, split the roles string into an array of permissions
  userPermissions = userInfoStore.userInfo.roles!.split(' ') as string[]
}

const hasPermission = (requiredPermissions: string[] | undefined) => {
  if (requiredPermissions === undefined) return true
  return requiredPermissions.every((permission) => userPermissions.includes(permission))
}
</script>

<style lang="scss" scoped></style>
