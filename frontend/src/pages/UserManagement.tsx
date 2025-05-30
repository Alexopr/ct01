import React, { useEffect, useState } from "react";
import { Card, Button, Input, Alert, Modal, Chip } from "../components/ui";
import { Icon } from '@iconify/react';
import { Select, SelectItem } from "@heroui/react";
import { getUsers, updateUser, deleteUser } from "../services/userService";

interface User {
  id: number;
  username: string;
  email: string;
  roles: string[];
}

const UserManagement: React.FC = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(false);
  const [search, setSearch] = useState("");
  const [editUser, setEditUser] = useState<User | null>(null);
  const [open, setOpen] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [deleteConfirm, setDeleteConfirm] = useState<number | null>(null);

  useEffect(() => {
    fetchUsers();
  }, []);

  const fetchUsers = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await getUsers();
      setUsers(data);
    } catch (e: any) {
      setError(e.message || "Ошибка загрузки пользователей");
    }
    setLoading(false);
  };

  const handleEdit = (user: User) => {
    setEditUser({ ...user });
    setOpen(true);
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteUser(id);
      fetchUsers();
      setDeleteConfirm(null);
    } catch (e: any) {
      setError(e.message || "Ошибка удаления");
    }
  };

  const handleSave = async () => {
    if (editUser) {
      try {
        await updateUser(editUser.id, editUser);
        setOpen(false);
        setEditUser(null);
        fetchUsers();
      } catch (e: any) {
        setError(e.message || "Ошибка сохранения");
      }
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
      case 'USER':
        return 'primary';
      default:
        return 'default';
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
            <p className="text-foreground-600">Редактирование и управление учетными записями</p>
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
              glassmorphism
              leftIcon="solar:magnifer-zoom-in-bold"
              className="w-full"
            />
          </div>
          
          <Button
            variant="primary"
            size="md"
            icon="solar:refresh-bold"
            onClick={fetchUsers}
            disabled={loading}
            gradient
          >
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
          variant="glass"
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
                <div className="hidden md:grid grid-cols-5 gap-4 p-4 bg-foreground-50 rounded-lg mb-4 text-sm font-semibold text-foreground-700">
                  <div>ID</div>
                  <div>Пользователь</div>
                  <div>Email</div>
                  <div>Роли</div>
                  <div>Действия</div>
                </div>

                {/* Table Body */}
                <div className="space-y-3">
                  {filteredUsers.map((user, index) => (
                    <div
                      key={user.id}
                      className="grid grid-cols-1 md:grid-cols-5 gap-4 p-4 bg-background/50 rounded-lg border border-divider/20 hover:bg-background/70 transition-colors duration-200 animate-in fade-in-0 slide-in-from-left-4"
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
                              icon="solar:pen-bold"
                              onClick={() => handleEdit(user)}
                            >
                              Изменить
                            </Button>
                            <Button
                              variant="ghost"
                              size="sm"
                              icon="solar:trash-bin-trash-bold"
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
                        <div className="flex gap-1">
                          {user.roles.map((role) => (
                            <Chip
                              key={role}
                              color={getRoleColor(role)}
                              size="sm"
                            >
                              {role}
                            </Chip>
                          ))}
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
                      <div className="hidden md:flex gap-1">
                        {user.roles.map((role) => (
                          <Chip
                            key={role}
                            color={getRoleColor(role)}
                            size="sm"
                          >
                            {role}
                          </Chip>
                        ))}
                      </div>
                      <div className="hidden md:flex gap-2">
                        <Button
                          variant="ghost"
                          size="sm"
                          icon="solar:pen-bold"
                          onClick={() => handleEdit(user)}
                        >
                          Изменить
                        </Button>
                        <Button
                          variant="ghost"
                          size="sm"
                          icon="solar:trash-bin-trash-bold"
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
              glassmorphism
            />
            
            <Input
              label="Email"
              type="email"
              value={editUser?.email || ""}
              onChange={(e) => setEditUser(editUser ? { ...editUser, email: e.target.value } : null)}
              variant="bordered"
              glassmorphism
            />
            
            <Select
              label="Роли"
              placeholder="Выберите роли"
              selectionMode="multiple"
              selectedKeys={editUser?.roles || []}
              onSelectionChange={(keys) => {
                const rolesArray = Array.from(keys) as string[];
                setEditUser(editUser ? { ...editUser, roles: rolesArray } : null);
              }}
              variant="bordered"
              className="w-full"
            >
              <SelectItem key="USER">USER</SelectItem>
              <SelectItem key="ADMIN">ADMIN</SelectItem>
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
              variant="primary"
              onClick={handleSave}
              gradient
              className="flex-1"
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
              variant="danger"
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