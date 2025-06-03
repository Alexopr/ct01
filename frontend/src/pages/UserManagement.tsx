import React, { useEffect, useState } from "react";
import { Card, Button, Input, Alert, Modal, Chip } from "../components/ui";
import { Icon } from '@iconify/react';
import { Select, SelectItem } from "@nextui-org/react";
import { updateUser, deleteUser, assignRole, removeRole, getRoles } from "../services/userService";
import { apiService } from '../services/api';
import type { User, UserFormData, Role } from '../types/api';

const UserManagement: React.FC = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [roles, setRoles] = useState<Role[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");
  const [editUser, setEditUser] = useState<{id: number} & Partial<UserFormData> | null>(null);
  const [open, setOpen] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [deleteConfirm, setDeleteConfirm] = useState<number | null>(null);
  const [roleActionLoading, setRoleActionLoading] = useState<{userId: number, roleId: number} | null>(null);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [usersResponse, rolesResponse] = await Promise.all([
        apiService.getUsers(),
        getRoles()
      ]);
      setUsers(usersResponse.content);
      setRoles(rolesResponse);
    } catch (e: any) {
      setError(e.message || "Ошибка загрузки данных");
    }
    setLoading(false);
  };

  const handleEdit = (user: User) => {
    const userFormData = {
      id: user.id,
      username: user.username,
      email: user.email,
      firstName: user.firstName,
      lastName: user.lastName,
      roles: user.roles.map(role => role.id)
    };
    setEditUser(userFormData);
    setOpen(true);
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteUser(id);
      fetchData();
      setDeleteConfirm(null);
    } catch (e: any) {
      setError(e.message || "Ошибка удаления");
    }
  };

  const handleSave = async () => {
    if (editUser && editUser.id) {
      try {
        const { id, ...userData } = editUser;
        await updateUser(id, userData);
        setOpen(false);
        setEditUser(null);
        fetchData();
      } catch (e: any) {
        setError(e.message || "Ошибка сохранения");
      }
    }
  };

  const handleQuickRoleToggle = async (userId: number, roleId: number, hasRole: boolean) => {
    setRoleActionLoading({ userId, roleId });
    try {
      if (hasRole) {
        await removeRole(userId, roleId);
      } else {
        await assignRole(userId, roleId);
      }
      fetchData();
    } catch (e: any) {
      setError(e.message || "Ошибка изменения роли");
    } finally {
      setRoleActionLoading(null);
    }
  };

  const filteredUsers = users.filter(u =>
    u.username.toLowerCase().includes(search.toLowerCase()) ||
    u.email.toLowerCase().includes(search.toLowerCase())
  );

  const getRoleColor = (role: string): 'default' | 'primary' | 'secondary' | 'success' | 'warning' | 'danger' => {
    switch (role) {
      case 'ADMIN':
        return 'danger';
      case 'PREMIUM':
        return 'warning';
      case 'MODERATOR':
        return 'secondary';
      case 'USER':
        return 'primary';
      default:
        return 'default';
    }
  };

  const getRoleDisplayName = (roleName: string): string => {
    switch (roleName) {
      case 'ADMIN':
        return 'Администратор';
      case 'PREMIUM':
        return 'Премиум';
      case 'MODERATOR':
        return 'Модератор';
      case 'USER':
        return 'Пользователь';
      default:
        return roleName;
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-background via-background/95 to-background/90 p-6">
      <div className="max-w-7xl mx-auto space-y-8 animate-in fade-in-0 slide-in-from-bottom-4 duration-1000">
        
        {/* Header */}
        <div className="flex items-center gap-4">
          <div className="inline-flex items-center justify-center w-12 h-12 bg-gradient-to-r from-primary to-secondary rounded-xl">
            <Icon icon="solar:users-group-rounded-bold" className="w-6 h-6 text-white" />
          </div>
          <div>
            <h1 className="text-3xl font-bold bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">
              Управление пользователями
            </h1>
            <p className="text-foreground-600">Редактирование и управление учетными записями и ролями</p>
          </div>
        </div>

        {/* Controls */}
        <div className="flex flex-col md:flex-row gap-4 items-center justify-between">
          <div className="w-full md:w-96">
            <Input
              placeholder="Поиск пользователей..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              variant="bordered"
              startContent={<Icon icon="solar:magnifer-zoom-in-bold" className="w-4 h-4" />}
              className="w-full"
            />
          </div>
          
          <Button
            color="primary"
            size="md"
            startContent={<Icon icon="solar:refresh-bold" className="w-4 h-4" />}
            onClick={fetchData}
            disabled={loading} >
            Обновить
          </Button>
        </div>

        {error && (
          <Alert
            type="error"
            title="Ошибка"
            description={error}
            closable
            onClose={() => setError(null)}
            className="animate-in fade-in-0 slide-in-from-top-4 duration-500"
          />
        )}

        {/* Users Table */}
        <Card
          
          className="backdrop-blur-xl bg-background/30 border border-divider/20 shadow-xl animate-in fade-in-0 slide-in-from-bottom-4 duration-700"
        >
          <div className="p-6">
            <div className="flex items-center justify-between mb-6">
              <div className="flex items-center gap-3">
                <Icon icon="solar:users-group-rounded-bold" className="w-6 h-6 text-primary" />
                <h2 className="text-xl font-semibold text-foreground">
                  Пользователи ({filteredUsers.length})
                </h2>
              </div>
            </div>

            {loading ? (
              <div className="space-y-4">
                {[1, 2, 3, 4, 5].map((i) => (
                  <div key={i} className="animate-pulse">
                    <div className="h-16 bg-foreground-200 rounded-lg"></div>
                  </div>
                ))}
              </div>
            ) : (
              <>
                {/* Table Header */}
                <div className="hidden md:grid grid-cols-6 gap-4 p-4 bg-foreground-50 rounded-lg mb-4 text-sm font-semibold text-foreground-700">
                  <div>ID</div>
                  <div>Пользователь</div>
                  <div>Email</div>
                  <div>Роли</div>
                  <div>Управление ролями</div>
                  <div>Действия</div>
                </div>

                {/* Table Body */}
                <div className="space-y-3">
                  {filteredUsers.map((user, index) => (
                    <div
                      key={user.id}
                      className="grid grid-cols-1 md:grid-cols-6 gap-4 p-4 bg-background/50 rounded-lg border border-divider/20 hover:bg-background/70 transition-colors duration-200 animate-in fade-in-0 slide-in-from-left-4"
                      style={{ animationDelay: `${index * 50}ms` }}
                    >
                      {/* Mobile Layout */}
                      <div className="md:hidden space-y-3">
                        <div className="flex items-center justify-between">
                          <span className="font-semibold text-foreground">#{user.id}</span>
                          <div className="flex gap-2">
                            <Button
                              variant="ghost"
                              size="sm"
                              startContent={<Icon icon="solar:pen-bold" className="w-4 h-4" />}
                              onClick={() => handleEdit(user)}
                            >
                              Изменить
                            </Button>
                            <Button
                              variant="ghost"
                              size="sm"
                              startContent={<Icon icon="solar:trash-bin-trash-bold" className="w-4 h-4" />}
                              onClick={() => setDeleteConfirm(user.id)}
                              className="text-danger hover:bg-danger/10"
                            >
                              Удалить
                            </Button>
                          </div>
                        </div>
                        <div>
                          <div className="font-medium text-foreground">{user.username}</div>
                          <div className="text-sm text-foreground-600">{user.email}</div>
                        </div>
                        <div className="space-y-2">
                          <div className="flex gap-1 flex-wrap">
                            {user.roles.map((role) => (
                              <Chip
                                key={role.id}
                                color={getRoleColor(role.name)}
                                size="sm"
                              >
                                {getRoleDisplayName(role.name)}
                              </Chip>
                            ))}
                          </div>
                          <div className="flex gap-1 flex-wrap">
                            {roles.map((role) => {
                              const hasRole = user.roles.some(ur => ur.id === role.id);
                              const isLoading = roleActionLoading?.userId === user.id && roleActionLoading?.roleId === role.id;
                              return (
                                <Button
                                  key={role.id}
                                  size="sm"
                                  variant={hasRole ? "solid" : "ghost"}
                                  color={hasRole ? getRoleColor(role.name) : "default"}
                                  onClick={() => handleQuickRoleToggle(user.id, role.id, hasRole)}
                                  disabled={isLoading}
                                  startContent={
                                    isLoading ? (
                                      <Icon icon="solar:refresh-bold" className="w-3 h-3 animate-spin" />
                                    ) : hasRole ? (
                                      <Icon icon="solar:check-circle-bold" className="w-3 h-3" />
                                    ) : (
                                      <Icon icon="solar:add-circle-bold" className="w-3 h-3" />
                                    )
                                  }
                                  className="text-xs"
                                >
                                  {getRoleDisplayName(role.name)}
                                </Button>
                              );
                            })}
                          </div>
                        </div>
                      </div>

                      {/* Desktop Layout */}
                      <div className="hidden md:block text-sm font-medium text-foreground">
                        #{user.id}
                      </div>
                      <div className="hidden md:block text-sm text-foreground">
                        {user.username}
                      </div>
                      <div className="hidden md:block text-sm text-foreground-600">
                        {user.email}
                      </div>
                      <div className="hidden md:flex gap-1 flex-wrap">
                        {user.roles.map((role) => (
                          <Chip
                            key={role.id}
                            color={getRoleColor(role.name)}
                            size="sm"
                          >
                            {getRoleDisplayName(role.name)}
                          </Chip>
                        ))}
                      </div>
                      <div className="hidden md:flex gap-1 flex-wrap">
                        {roles.map((role) => {
                          const hasRole = user.roles.some(ur => ur.id === role.id);
                          const isLoading = roleActionLoading?.userId === user.id && roleActionLoading?.roleId === role.id;
                          return (
                            <Button
                              key={role.id}
                              size="sm"
                              variant={hasRole ? "solid" : "ghost"}
                              color={hasRole ? getRoleColor(role.name) : "default"}
                              onClick={() => handleQuickRoleToggle(user.id, role.id, hasRole)}
                              disabled={isLoading}
                              startContent={
                                isLoading ? (
                                  <Icon icon="solar:refresh-bold" className="w-3 h-3 animate-spin" />
                                ) : hasRole ? (
                                  <Icon icon="solar:check-circle-bold" className="w-3 h-3" />
                                ) : (
                                  <Icon icon="solar:add-circle-bold" className="w-3 h-3" />
                                )
                              }
                              className="text-xs"
                            >
                              {getRoleDisplayName(role.name)}
                            </Button>
                          );
                        })}
                      </div>
                      <div className="hidden md:flex gap-2">
                        <Button
                          variant="ghost"
                          size="sm"
                          startContent={<Icon icon="solar:pen-bold" className="w-4 h-4" />}
                          onClick={() => handleEdit(user)}
                        >
                          Изменить
                        </Button>
                        <Button
                          variant="ghost"
                          size="sm"
                          startContent={<Icon icon="solar:trash-bin-trash-bold" className="w-4 h-4" />}
                          onClick={() => setDeleteConfirm(user.id)}
                          className="text-danger hover:bg-danger/10"
                        >
                          Удалить
                        </Button>
                      </div>
                    </div>
                  ))}
                </div>

                {filteredUsers.length === 0 && (
                  <div className="text-center py-12">
                    <Icon icon="solar:users-group-rounded-bold" className="w-16 h-16 text-foreground-400 mx-auto mb-4" />
                    <h3 className="text-lg font-semibold text-foreground mb-2">
                      Пользователи не найдены
                    </h3>
                    <p className="text-foreground-600">
                      {search ? 'Попробуйте изменить критерии поиска' : 'В системе пока нет пользователей'}
                    </p>
                  </div>
                )}
              </>
            )}
          </div>
        </Card>

        {/* Edit User Modal */}
        <Modal
          isOpen={open}
          onClose={() => {
            setOpen(false);
            setEditUser(null);
          }}
          title="Редактирование пользователя"
          size="md"
        >
          <div className="space-y-4">
            <Input
              label="Имя пользователя"
              value={editUser?.username || ""}
              onChange={(e) => setEditUser(editUser ? { ...editUser, username: e.target.value } : null)}
              variant="bordered"
            />
            
            <Input
              label="Email"
              type="email"
              value={editUser?.email || ""}
              onChange={(e) => setEditUser(editUser ? { ...editUser, email: e.target.value } : null)}
              variant="bordered"
            />

            <div className="grid grid-cols-2 gap-4">
              <Input
                label="Имя"
                value={editUser?.firstName || ""}
                onChange={(e) => setEditUser(editUser ? { ...editUser, firstName: e.target.value } : null)}
                variant="bordered"
              />
              
              <Input
                label="Фамилия"
                value={editUser?.lastName || ""}
                onChange={(e) => setEditUser(editUser ? { ...editUser, lastName: e.target.value } : null)}
                variant="bordered"
              />
            </div>
            
            <Select
              label="Роли"
              placeholder="Выберите роли"
              selectionMode="multiple"
              selectedKeys={editUser?.roles?.map(String) || []}
              onSelectionChange={(keys) => {
                const rolesArray = Array.from(keys).map(key => Number(key));
                setEditUser(editUser ? { ...editUser, roles: rolesArray } : null);
              }}
              variant="bordered"
              className="w-full"
            >
              {roles.map((role) => (
                <SelectItem key={role.id} value={role.id}>
                  {getRoleDisplayName(role.name)}
                </SelectItem>
              ))}
            </Select>
          </div>
          
          <div className="flex gap-3 mt-6">
            <Button
              variant="ghost"
              onClick={() => {
                setOpen(false);
                setEditUser(null);
              }}
              className="flex-1"
            >
              Отмена
            </Button>
            <Button
              color="primary"
              onClick={handleSave} className="flex-1"
            >
              Сохранить
            </Button>
          </div>
        </Modal>

        {/* Delete Confirmation Modal */}
        <Modal
          isOpen={deleteConfirm !== null}
          onClose={() => setDeleteConfirm(null)}
          title="Подтверждение удаления"
          size="sm"
        >
          <div className="space-y-4">
            <div className="flex items-center gap-3">
              <div className="w-12 h-12 bg-danger/20 rounded-full flex items-center justify-center">
                <Icon icon="solar:danger-triangle-bold" className="w-6 h-6 text-danger" />
              </div>
              <div>
                <p className="text-foreground font-medium">Удалить пользователя?</p>
                <p className="text-sm text-foreground-600">Это действие нельзя отменить</p>
              </div>
            </div>
          </div>
          
          <div className="flex gap-3 mt-6">
            <Button
              variant="ghost"
              onClick={() => setDeleteConfirm(null)}
              className="flex-1"
            >
              Отмена
            </Button>
            <Button
              color="danger"
              onClick={() => deleteConfirm && handleDelete(deleteConfirm)}
              className="flex-1"
            >
              Удалить
            </Button>
          </div>
        </Modal>
      </div>
    </div>
  );
};

export default UserManagement; 



