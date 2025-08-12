export function canModifyTodo(todo, currentUser) {
  if (!currentUser) return false;
  const isOwner = currentUser.id === todo.ownerId;
  const isAdmin = currentUser.roles?.includes('ROLE_ADMIN');
  return isOwner || isAdmin;
}