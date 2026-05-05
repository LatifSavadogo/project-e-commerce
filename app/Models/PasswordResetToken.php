<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class PasswordResetToken extends Model
{
    protected $table = 'PasswordResetToken';
    protected $primaryKey = 'id';
    public $timestamps = false;
    protected $guarded = [];
}
