import React, { useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import { getUserById } from "../services/userService";
import type { User } from "../services/userService";

const UserDetails: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;
    setLoading(true);
    getUserById(Number(id))
      .then(setUser)
      .catch(e => setError(e.message || "Ошибка загрузки"))
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) return <div>Загрузка...</div>;
  if (error) return <div className="text-red-600">{error}</div>;
  if (!user) return <div>Пользователь не найден</div>;

  return (
    <div className="p-4">
      <h2 className="text-xl font-bold mb-2">Пользователь: {user.username}</h2>
      <div>Email: {user.email}</div>
      <div>Роли: {user.roles.join(", ")}</div>
      <div>Создан: {user.createdAt}</div>
      <div>Обновлён: {user.updatedAt}</div>
      <Link to={`/users/${user.id}/edit`} className="text-yellow-600">Редактировать</Link>
    </div>
  );
};

export default UserDetails; 