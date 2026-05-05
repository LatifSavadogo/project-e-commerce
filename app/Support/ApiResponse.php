<?php

namespace App\Support;

class ApiResponse
{
    public static function ok(array $data = [], int $status = 200)
    {
        return response()->json($data, $status);
    }

    public static function error(string $message, string $code = 'BAD_REQUEST', int $status = 400)
    {
        return response()->json([
            'error' => $code,
            'message' => $message,
            'status' => $status,
        ], $status);
    }
}
