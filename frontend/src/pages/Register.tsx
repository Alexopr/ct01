import React, { useState } from "react";
import { createUser } from "../services/userService";

const Register: React.FC = () => {
  const [form, setForm] = useState({ username: "", password: "", email: "" });
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setSuccess(false);
    try {
      await createUser({ ...form, roles: ["USER"] });
      setSuccess(true);
      setForm({ username: "", password: "", email: "" });
    } catch (e: any) {
      setError(e.message || "Ошибка регистрации");
    }
  };

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-2 max-w-xs mx-auto mt-10">
      <input
        type="text"
        name="username"
        placeholder="Username"
        className="border p-2 rounded"
        required
        value={form.username}
        onChange={handleChange}
      />
      <input
        type="password"
        name="password"
        placeholder="Password"
        className="border p-2 rounded"
        required
        value={form.password}
        onChange={handleChange}
      />
      <input
        type="email"
        name="email"
        placeholder="Email"
        className="border p-2 rounded"
        required
        value={form.email}
        onChange={handleChange}
      />
      <button type="submit" className="bg-green-600 text-white p-2 rounded">Зарегистрироваться</button>
      {error && <div className="text-red-600 mt-2">{error}</div>}
      {success && <div className="text-green-600 mt-2">Регистрация успешна!</div>}
    </form>
  );
};

export default Register; 