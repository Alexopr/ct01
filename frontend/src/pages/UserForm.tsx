import React, { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { createUser, updateUser, getUserById } from "../services/userService";
import { Formik, Form } from "formik";
import * as Yup from "yup";
import { Box, Paper, Typography, TextField, Button, Stack, Alert, Skeleton } from "@mui/material";

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

  if (loading) return <Skeleton variant="rectangular" width="100%" height={320} sx={{ borderRadius: 4, mt: 4 }} />;
  if (error) return <Alert severity="error" sx={{ mt: 4 }}>{error}</Alert>;

  return (
    <Box
      sx={{
        minHeight: '80vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        px: 2,
        py: 8,
        background: 'linear-gradient(135deg, #23263a 0%, #2d314d 100%)',
        position: 'relative',
        overflow: 'hidden',
        animation: 'fadeIn 1s',
        '@keyframes fadeIn': {
          '0%': { opacity: 0, transform: 'translateY(32px)' },
          '100%': { opacity: 1, transform: 'none' },
        },
      }}
    >
      <Paper sx={{
        bgcolor: 'rgba(35,38,58,0.7)',
        backdropFilter: 'blur(16px)',
        WebkitBackdropFilter: 'blur(16px)',
        borderRadius: 6,
        boxShadow: '0 8px 40px 0 #6C47FF33, 0 2px 12px 0 #00E4FF22',
        px: { xs: 2, sm: 6 },
        py: { xs: 4, sm: 6 },
        maxWidth: 500,
        width: '100%',
        textAlign: 'center',
      }}>
        <Typography variant="h4" fontWeight={800} sx={{ color: 'primary.main', letterSpacing: 1.2, mb: 3, textShadow: '0 2px 16px #6C47FF33' }}>
          {id ? "Редактировать пользователя" : "Создать пользователя"}
        </Typography>
        <Formik
          initialValues={initialValues}
          enableReinitialize
          validationSchema={UserSchema}
          onSubmit={handleSubmit}
        >
          {({ values, handleChange, handleBlur, touched, errors, isSubmitting }) => (
            <Form>
              <Stack spacing={3}>
                <TextField
                  label="Username"
                  name="username"
                  value={values.username}
                  onChange={handleChange}
                  onBlur={handleBlur}
                  error={touched.username && Boolean(errors.username)}
                  helperText={touched.username && errors.username}
                  fullWidth
                  required
                  sx={{ input: { color: '#fff' } }}
                />
                <TextField
                  label="Email"
                  name="email"
                  value={values.email}
                  onChange={handleChange}
                  onBlur={handleBlur}
                  error={touched.email && Boolean(errors.email)}
                  helperText={touched.email && errors.email}
                  fullWidth
                  required
                  sx={{ input: { color: '#fff' } }}
                />
                <TextField
                  label="First Name"
                  name="firstName"
                  value={values.firstName}
                  onChange={handleChange}
                  onBlur={handleBlur}
                  error={touched.firstName && Boolean(errors.firstName)}
                  helperText={touched.firstName && errors.firstName}
                  fullWidth
                  sx={{ input: { color: '#fff' } }}
                />
                <TextField
                  label="Last Name"
                  name="lastName"
                  value={values.lastName}
                  onChange={handleChange}
                  onBlur={handleBlur}
                  error={touched.lastName && Boolean(errors.lastName)}
                  helperText={touched.lastName && errors.lastName}
                  fullWidth
                  sx={{ input: { color: '#fff' } }}
                />
                <TextField
                  label="Роли (через запятую)"
                  name="roles"
                  value={values.roles}
                  onChange={handleChange}
                  onBlur={handleBlur}
                  error={touched.roles && Boolean(errors.roles)}
                  helperText={touched.roles && errors.roles}
                  fullWidth
                  required
                  sx={{ input: { color: '#fff' } }}
                />
                <Stack direction="row" spacing={2} justifyContent="flex-end" mt={2}>
                  <Button
                    variant="contained"
                    color="primary"
                    type="submit"
                    disabled={isSubmitting}
                    sx={{
                      fontWeight: 700,
                      fontSize: 18,
                      borderRadius: 4,
                      background: 'linear-gradient(135deg, #6C47FF 0%, #00E4FF 100%)',
                      boxShadow: '0 4px 24px 0 #6C47FF33',
                      transition: 'background 0.2s, box-shadow 0.2s',
                      '&:hover': {
                        background: 'linear-gradient(135deg, #00E4FF 0%, #6C47FF 100%)',
                        boxShadow: '0 8px 32px 0 #00E4FF33',
                      },
                    }}
                  >
                    {id ? "Сохранить" : "Создать"}
                  </Button>
                  <Button variant="outlined" color="secondary" onClick={() => navigate("/users")}>Отмена</Button>
                </Stack>
              </Stack>
            </Form>
          )}
        </Formik>
      </Paper>
    </Box>
  );
};

export default UserForm; 