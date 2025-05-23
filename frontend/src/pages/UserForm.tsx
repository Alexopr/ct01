import React, { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { createUser, updateUser, getUserById } from "../services/userService";
import { Formik, Form, Field, ErrorMessage } from "formik";
import * as Yup from "yup";

const UserSchema = Yup.object().shape({
  username: Yup.string().required("Обязательное поле"),
  email: Yup.string().email("Некорректный email").required("Обязательное поле"),
  firstName: Yup.string(),
  lastName: Yup.string(),
  roles: Yup.string().required("Обязательное поле"),
});

const UserForm: React.FC = () => {
  const { id } = useParams<{ id?: string }>();
  const navigate = useNavigate();
  const [initialValues, setInitialValues] = useState({
    username: "",
    email: "",
    firstName: "",
    lastName: "",
    roles: "USER",
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (id) {
      setLoading(true);
      getUserById(Number(id))
        .then(user => setInitialValues({
          username: user.username,
          email: user.email,
          firstName: user.firstName || "",
          lastName: user.lastName || "",
          roles: user.roles.join(","),
        }))
        .catch(e => setError(e.message || "Ошибка загрузки"))
        .finally(() => setLoading(false));
    }
  }, [id]);

  const handleSubmit = async (values: any) => {
    setLoading(true);
    try {
      if (id) {
        await updateUser(Number(id), {
          ...values,
          roles: values.roles.split(",").map((r: string) => r.trim()),
        });
      } else {
        await createUser({
          ...values,
          roles: values.roles.split(",").map((r: string) => r.trim()),
        });
      }
      navigate("/users");
    } catch (e: any) {
      setError(e.message || "Ошибка сохранения");
    }
    setLoading(false);
  };

  if (loading) return <div>Загрузка...</div>;
  if (error) return <div className="text-red-600">{error}</div>;

  return (
    <div className="p-4 max-w-md mx-auto">
      <h2 className="text-xl font-bold mb-4">{id ? "Редактировать" : "Создать"} пользователя</h2>
      <Formik
        initialValues={initialValues}
        enableReinitialize
        validationSchema={UserSchema}
        onSubmit={handleSubmit}
      >
        <Form className="flex flex-col gap-2">
          <label>Username<Field name="username" className="border p-2 rounded w-full" /></label>
          <ErrorMessage name="username" component="div" className="text-red-600" />
          <label>Email<Field name="email" className="border p-2 rounded w-full" /></label>
          <ErrorMessage name="email" component="div" className="text-red-600" />
          <label>First Name<Field name="firstName" className="border p-2 rounded w-full" /></label>
          <ErrorMessage name="firstName" component="div" className="text-red-600" />
          <label>Last Name<Field name="lastName" className="border p-2 rounded w-full" /></label>
          <ErrorMessage name="lastName" component="div" className="text-red-600" />
          <label>Roles (через запятую)<Field name="roles" className="border p-2 rounded w-full" /></label>
          <ErrorMessage name="roles" component="div" className="text-red-600" />
          <button type="submit" className="bg-blue-600 text-white p-2 rounded mt-2" disabled={loading}>
            {loading ? "Сохранение..." : "Сохранить"}
          </button>
        </Form>
      </Formik>
    </div>
  );
};

export default UserForm; 