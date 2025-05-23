import React, { useEffect, useState } from "react";
import { getUsers, deleteUser } from "../services/userService";
import type { User } from "../services/userService";
import { Link } from "react-router-dom";

const UserList: React.FC = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const data = await getUsers();
      setUsers(data);
      setError(null);
    } catch (e: any) {
      setError(e.message || "Ошибка загрузки пользователей");
    }
    setLoading(false);
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  const handleDelete = async (id: number) => {
    if (!window.confirm("Удалить пользователя?")) return;
    try {
      await deleteUser(id);
      setUsers(users.filter(u => u.id !== id));
    } catch (e: any) {
      alert(e.message || "Ошибка удаления");
    }
  };

  if (loading) return <div>Загрузка...</div>;
  if (error) return <div className="text-red-600">{error}</div>;

  return (
    <div className="p-4">
      <div className="flex justify-between mb-4">
        <h2 className="text-xl font-bold">Пользователи</h2>
        <Link to="/users/new" className="bg-green-600 text-white px-4 py-2 rounded">Добавить</Link>
      </div>
      <table className="min-w-full border">
        <thead>
          <tr>
            <th className="border px-2">ID</th>
            <th className="border px-2">Username</th>
            <th className="border px-2">Email</th>
            <th className="border px-2">Роли</th>
            <th className="border px-2">Действия</th>
          </tr>
        </thead>
        <tbody>
          {users.map(user => (
            <tr key={user.id}>
              <td className="border px-2">{user.id}</td>
              <td className="border px-2">{user.username}</td>
              <td className="border px-2">{user.email}</td>
              <td className="border px-2">{user.roles.join(", ")}</td>
              <td className="border px-2 flex gap-2">
                <Link to={`/users/${user.id}`} className="text-blue-600">Просмотр</Link>
                <Link to={`/users/${user.id}/edit`} className="text-yellow-600">Редактировать</Link>
                <button onClick={() => handleDelete(user.id)} className="text-red-600">Удалить</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default UserList; 