import React, { useEffect, useState } from "react";
import { DataGrid, GridActionsCellItem } from "@mui/x-data-grid";
import type { GridColDef } from "@mui/x-data-grid";
import { Button, Dialog, DialogTitle, DialogContent, DialogActions, TextField, MenuItem, Select, InputLabel, FormControl } from "@mui/material";
import { getUsers, updateUser, deleteUser } from "../services/userService";
import { Typography } from "@mui/material";

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

  useEffect(() => {
    fetchUsers();
  }, []);

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const data = await getUsers();
      setUsers(data);
    } finally {
      setLoading(false);
    }
  };

  const handleEdit = (user: User) => {
    setEditUser(user);
    setOpen(true);
  };

  const handleDelete = async (id: number) => {
    await deleteUser(id);
    fetchUsers();
  };

  const handleSave = async () => {
    if (editUser) {
      await updateUser(editUser.id, editUser);
      setOpen(false);
      setEditUser(null);
      fetchUsers();
    }
  };

  const filteredUsers = users.filter(u =>
    u.username.toLowerCase().includes(search.toLowerCase()) ||
    u.email.toLowerCase().includes(search.toLowerCase())
  );

  const columns: GridColDef[] = [
    { field: "id", headerName: "ID", width: 70 },
    { field: "username", headerName: "Username", width: 150 },
    { field: "email", headerName: "Email", width: 200 },
    { field: "roles", headerName: "Roles", width: 150, valueGetter: (params: any) => (params.row?.roles ? params.row.roles.join(", ") : "") },
    {
      field: "actions",
      type: "actions",
      width: 120,
      getActions: (params: any) => [
        <GridActionsCellItem label="Edit" onClick={() => handleEdit(params.row)} showInMenu />, 
        <GridActionsCellItem label="Delete" onClick={() => handleDelete(params.row.id)} showInMenu />
      ]
    }
  ];

  return (
    <div style={{ height: 500, width: "100%" }}>
      <Typography variant="h5" sx={{ mb: 2, fontSize: { xs: '1.2rem', sm: '1.5rem', md: '2rem' }, fontWeight: 600 }}>
        User Management
      </Typography>
      <TextField
        label="Search"
        value={search}
        onChange={e => setSearch(e.target.value)}
        style={{ marginBottom: 16 }}
      />
      <DataGrid
        rows={filteredUsers}
        columns={columns}
        initialState={{
          pagination: { paginationModel: { pageSize: 10, page: 0 } }
        }}
        pageSizeOptions={[10, 20, 50]}
        loading={loading}
        disableRowSelectionOnClick
        autoHeight
      />
      <Dialog open={open} onClose={() => setOpen(false)}>
        <DialogTitle>Edit User</DialogTitle>
        <DialogContent>
          <TextField
            label="Username"
            value={editUser?.username || ""}
            onChange={e => setEditUser(editUser ? { ...editUser, username: e.target.value } : null)}
            fullWidth
            margin="normal"
          />
          <TextField
            label="Email"
            value={editUser?.email || ""}
            onChange={e => setEditUser(editUser ? { ...editUser, email: e.target.value } : null)}
            fullWidth
            margin="normal"
          />
          <FormControl fullWidth margin="normal">
            <InputLabel>Roles</InputLabel>
            <Select
              multiple
              value={editUser?.roles || []}
              onChange={e => setEditUser(editUser ? { ...editUser, roles: e.target.value as string[] } : null)}
              label="Roles"
            >
              <MenuItem value="USER">USER</MenuItem>
              <MenuItem value="ADMIN">ADMIN</MenuItem>
            </Select>
          </FormControl>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)}>Cancel</Button>
          <Button onClick={handleSave} variant="contained">Save</Button>
        </DialogActions>
      </Dialog>
    </div>
  );
};

export default UserManagement; 