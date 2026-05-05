<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class ChatMessage extends Model
{
    protected $table = 'ChatMessage';
    protected $primaryKey = 'idmessage';
    public $timestamps = false;
    protected $guarded = [];
}
