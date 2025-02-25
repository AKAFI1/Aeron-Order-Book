package org.weareadaptive.domain.repository;

import org.agrona.collections.Long2ObjectHashMap;
import org.weareadaptive.domain.dto.User;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class UserRepository
{
    private final ConcurrentHashMap<String, User> usersByUsername = new ConcurrentHashMap<>();
    private final Long2ObjectHashMap<User> usersById = new Long2ObjectHashMap<>();
    private final AtomicLong userIdCounter = new AtomicLong(1);

    public long getOrCreateUserId(final String username)
    {
        return usersByUsername.computeIfAbsent(username, key ->
        {
            final long userId = userIdCounter.getAndIncrement();
            final User user = new User(userId, username);
            usersById.put(userId, user);
            return user;
        }).userId();
    }

    public User getUserById(final long userId)
    {
        return usersById.get(userId);
    }

    public User getUserByUsername(final String username)
    {
        return usersByUsername.get(username);
    }

    public List<User> getAllUsers()
    {
        return usersById.values().stream().toList();
    }

    public void put(final long userId, final User user)
    {
        usersById.put(userId, user);
    }
}
