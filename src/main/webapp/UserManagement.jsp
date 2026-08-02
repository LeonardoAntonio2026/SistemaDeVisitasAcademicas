<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    request.setAttribute("pageTitle", "Gestión de usuarios");
    String role = request.getParameter("role");
    boolean isAdmin = "admin".equalsIgnoreCase(role);
%>
<%@ include file="layout/header.jsp" %>
<%@ include file="layout/sidebar.jsp" %>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/user-management.css">

<main id="main-content">
    <% if (!isAdmin) { %>
    <section class="access-card">
        <h2>Acceso restringido</h2>
        <p>Este panel solo está disponible para usuarios con permisos de administrador.</p>
        <a class="btn-primary" href="${pageContext.request.contextPath}/UserManagement.jsp?role=admin">Ingresar como administrador</a>
    </section>
    <% } else { %>
    <section class="management-shell">
        <div class="page-header">
            <div>
                <p class="eyebrow">Panel de administración</p>
                <h2>Gestión de usuarios</h2>
                <p class="subtitle">Administra docentes y usuarios del sistema desde un único panel.</p>
            </div>
            <div class="header-badge">Solo administrador</div>
        </div>

        <div class="summary-grid">
            <article class="summary-card">
                <span class="summary-label">Usuarios activos</span>
                <strong id="activeUsersCount">0</strong>
            </article>
            <article class="summary-card">
                <span class="summary-label">Administradores</span>
                <strong id="adminUsersCount">0</strong>
            </article>
            <article class="summary-card">
                <span class="summary-label">Docentes</span>
                <strong id="teacherUsersCount">0</strong>
            </article>
        </div>

        <div class="card-panel">
            <div class="card-panel-header">
                <div>
                    <h3 id="formTitle">Agregar usuario</h3>
                    <p>Completa los datos para crear o actualizar un usuario.</p>
                </div>
                <button type="button" class="btn-secondary" id="resetFormBtn">Limpiar formulario</button>
            </div>

            <form id="userForm" class="user-form">
                <input type="hidden" id="userId" name="userId">
                <div class="form-grid">
                    <div class="field-group">
                        <label for="fullName">Nombre completo</label>
                        <input id="fullName" name="fullName" type="text" placeholder="Ej. Ana López" required>
                    </div>
                    <div class="field-group">
                        <label for="email">Correo electrónico</label>
                        <input id="email" name="email" type="email" placeholder="usuario@utez.edu.mx" required>
                    </div>
                    <div class="field-group">
                        <label for="role">Rol</label>
                        <select id="role" name="role" required>
                            <option value="">Selecciona un rol</option>
                            <option value="Administrador">Administrador</option>
                            <option value="Docente">Docente</option>
                            <option value="Coordinador">Coordinador</option>
                        </select>
                    </div>
                    <div class="field-group">
                        <label for="status">Estado</label>
                        <select id="status" name="status" required>
                            <option value="Activo">Activo</option>
                            <option value="Inactivo">Inactivo</option>
                        </select>
                    </div>
                </div>
                <div class="form-actions">
                    <button type="submit" class="btn-primary">Guardar usuario</button>
                </div>
            </form>
        </div>

        <div class="card-panel">
            <div class="card-panel-header">
                <div>
                    <h3>Usuarios registrados</h3>
                    <p>Lista actual del sistema con opciones de edición y eliminación.</p>
                </div>
            </div>

            <div class="table-responsive">
                <table class="user-table">
                    <thead>
                        <tr>
                            <th>Nombre</th>
                            <th>Correo</th>
                            <th>Rol</th>
                            <th>Estado</th>
                            <th>Acciones</th>
                        </tr>
                    </thead>
                    <tbody id="userTableBody"></tbody>
                </table>
            </div>
        </div>
    </section>
    <% } %>
</main>
</div>
</body>
<script>
    const STORAGE_KEY = 'userManagementUsers';
    const initialUsers = [
        { id: 1, fullName: 'Leonardo Antonio', email: 'leonardo@utez.edu.mx', role: 'Administrador', status: 'Activo' },
        { id: 2, fullName: 'Patricia Ruiz', email: 'patricia@utez.edu.mx', role: 'Docente', status: 'Activo' },
        { id: 3, fullName: 'Carlos Méndez', email: 'carlos@utez.edu.mx', role: 'Coordinador', status: 'Inactivo' }
    ];

    const form = document.getElementById('userForm');
    const tableBody = document.getElementById('userTableBody');
    const formTitle = document.getElementById('formTitle');
    const resetBtn = document.getElementById('resetFormBtn');
    const userIdInput = document.getElementById('userId');

    const state = {
        users: JSON.parse(localStorage.getItem(STORAGE_KEY) || 'null') || initialUsers,
        editingId: null
    };

    function saveUsers() {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(state.users));
        renderUsers();
    }

    function renderUsers() {
        tableBody.innerHTML = '';
        if (!state.users.length) {
            tableBody.innerHTML = '<tr><td colspan="5" class="empty-state">No hay usuarios registrados.</td></tr>';
            updateSummary();
            return;
        }

        state.users.forEach((user) => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${user.fullName}</td>
                <td>${user.email}</td>
                <td>${user.role}</td>
                <td><span class="status-badge ${user.status.toLowerCase()}">${user.status}</span></td>
                <td>
                    <div class="action-buttons">
                        <button type="button" class="btn-edit" data-id="${user.id}">Editar</button>
                        <button type="button" class="btn-delete" data-id="${user.id}">Eliminar</button>
                    </div>
                </td>
            `;
            tableBody.appendChild(row);
        });

        updateSummary();
    }

    function updateSummary() {
        const active = state.users.filter(user => user.status === 'Activo').length;
        const admins = state.users.filter(user => user.role === 'Administrador').length;
        const teachers = state.users.filter(user => user.role === 'Docente').length;
        document.getElementById('activeUsersCount').textContent = active;
        document.getElementById('adminUsersCount').textContent = admins;
        document.getElementById('teacherUsersCount').textContent = teachers;
    }

    form.addEventListener('submit', function (event) {
        event.preventDefault();
        const formData = new FormData(form);
        const userData = {
            id: Number(userIdInput.value) || Date.now(),
            fullName: formData.get('fullName').toString().trim(),
            email: formData.get('email').toString().trim(),
            role: formData.get('role').toString(),
            status: formData.get('status').toString()
        };

        if (!userData.fullName || !userData.email || !userData.role) {
            return;
        }

        if (state.editingId) {
            state.users = state.users.map(user => user.id === state.editingId ? userData : user);
            state.editingId = null;
        } else {
            state.users.unshift(userData);
        }

        saveUsers();
        form.reset();
        userIdInput.value = '';
        formTitle.textContent = 'Agregar usuario';
    });

    resetBtn.addEventListener('click', function () {
        form.reset();
        userIdInput.value = '';
        state.editingId = null;
        formTitle.textContent = 'Agregar usuario';
    });

    tableBody.addEventListener('click', function (event) {
        const target = event.target;
        if (target.tagName !== 'BUTTON') return;

        const userId = Number(target.getAttribute('data-id'));
        const user = state.users.find(item => item.id === userId);
        if (!user) return;

        if (target.classList.contains('btn-edit')) {
            state.editingId = user.id;
            userIdInput.value = user.id;
            document.getElementById('fullName').value = user.fullName;
            document.getElementById('email').value = user.email;
            document.getElementById('role').value = user.role;
            document.getElementById('status').value = user.status;
            formTitle.textContent = 'Actualizar usuario';
            document.getElementById('fullName').focus();
        }

        if (target.classList.contains('btn-delete')) {
            const confirmDelete = confirm(`¿Deseas eliminar a ${user.fullName}?`);
            if (confirmDelete) {
                state.users = state.users.filter(item => item.id !== userId);
                saveUsers();
                if (state.editingId === userId) {
                    state.editingId = null;
                    form.reset();
                    userIdInput.value = '';
                    formTitle.textContent = 'Agregar usuario';
                }
            }
        }
    });

    renderUsers();
</script>
</html>
